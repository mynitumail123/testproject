package com.aetna.chs.marketingdam.servlets;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.collections4.IterableUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.SearchResult;
import com.day.cq.commons.date.DateUtil;
import com.day.cq.dam.commons.util.DamUtil;
import com.adobe.xfa.Node;
import com.aetna.chs.marketingdam.models.impl.*;

import org.apache.sling.api.resource.Resource;

@Component(service = Servlet.class, property = { "sling.servlet.paths=/bin/testq", "sling.servlet.methods=GET" })
public class QueryServlet extends SlingAllMethodsServlet {

	ResourceResolver resourceResolver = null;
	@Reference
	private ResourceResolverFactory resolverFactory;

	@Reference
	private QueryBuilder builder;

	private Session session;

	Map<String, Object> paramMap = new HashMap<String, Object>();

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryServlet.class);

	// final static String ISO8601_DATE = ("YYYY-MM-DDTHH:mm:ss.SSSZ");
	@Override
	protected void doGet(SlingHttpServletRequest req, SlingHttpServletResponse resp)
			throws ServletException, IOException {

		LOGGER.info("SaveImageToDamServlet ::: do Get 444444444444");
		try {
			paramMap.put(ResourceResolverFactory.SUBSERVICE, "aetnahomeuser");
			resourceResolver = resolverFactory.getServiceResourceResolver(paramMap);
			session = resourceResolver.adaptTo(Session.class);
			DateUtil dates = new DateUtil();

			Calendar startDate = dates.getToday();
			Calendar endDate = dates.getToday();
			Calendar startDate1 = dates.getToday();

			// LOGGER.debug("TestMain.getFutureDate()" +
			// DateUtil.getISO8601DateNoTime(startDate));

			Map<String, String> map = new HashMap<String, String>();
			//List<DamHomeUser> userList = getDownLoadAssetList(startDate1, endDate);
			//LOGGER.debug("download asset Search Results: " + sr1.getTotalMatches());
			SearchResult searchResult = getExpiredAssetList(startDate, endDate);
			//resp.getWriter().print("Asset count is " + userList.size() + session.getUserID() + searchResult.getTotalMatches());
			resp.getWriter().print("Asset count is " + searchResult.getTotalMatches() + "User Id --> "+ session.getUserID());
		} catch (Exception e) {
			LOGGER.error("Errrrrrrrrrrrrrrrrrrrrrrrrrr" + e.getMessage());
		}

	}

	public SearchResult getExpiredAssetList(Calendar startDate, Calendar endDate) {

		Map<String, String> map = new HashMap<String, String>();

		// map.put("type", "dam:Asset");
		map.put("path", "/content/dam");
		startDate.add(Calendar.DAY_OF_MONTH, 2);
		endDate.add(Calendar.DAY_OF_MONTH, 8);
		map.put("group.1_daterange.property", "prism:expirationDate");
		map.put("group.1_daterange.lowerBound", DateUtil.getISO8601DateNoTime(startDate));
		map.put("group.1_daterange.lowerOperation", ">=");
		map.put("group.1_daterange.upperBound", DateUtil.getISO8601DateNoTime(endDate));
		map.put("group.1_daterange.upperOperation", "<=");
		map.put("p.guessTotal", "true");
		map.put("p.limit", "-1");

		LOGGER.debug("Start ============== Date {}", DateUtil.getISO8601DateNoTime(startDate));
		LOGGER.debug("end ============== Date {}", DateUtil.getISO8601DateNoTime(endDate));

		builder = resourceResolver.adaptTo(QueryBuilder.class);
		Query query = builder.createQuery(PredicateGroup.create(map), session);

		SearchResult sr = query.getResult();
		return sr;
	}
/*
	public List<DamHomeUser> getDownLoadAssetList(Calendar p_startDate, Calendar endDate) {
		SearchResult searchResult = null;
		List<DamHomeUser> homeUser = new ArrayList<DamHomeUser>() ;
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("path", "/home/users");
			p_startDate.add(Calendar.YEAR, -3);
			map.put("daterange.property", "jcr:created");
			map.put("daterange.lowerBound", DateUtil.getISO8601DateNoTime(p_startDate));
			map.put("daterange.lowerOperation", ">=");
			map.put("daterange.upperBound", DateUtil.getISO8601DateNoTime(endDate));
			map.put("orderby", "@jcr:created");
			map.put("daterange.upperOperation", "<=");
			map.put("property", "verb");
			map.put("property.value", "DOWNLOADED");

			builder = resourceResolver.adaptTo(QueryBuilder.class);
			Query query = builder.createQuery(PredicateGroup.create(map), session);

			searchResult = query.getResult();
			 homeUser = getDamHomeUserList(searchResult);
		} catch (Exception e) {
			LOGGER.debug("Errrrrrrrrrrrrrrrrrrrrrrrrrrrrrr {}", e.getMessage());
		}
		return homeUser;
	}

	public List<DamHomeUser> getDamHomeUserList(SearchResult searchResult) {
		LOGGER.debug("QueryServlet.getDamHomeUserList() result size {}", searchResult.getTotalMatches());
		List<DamHomeUser> damHomeUserList = new ArrayList<DamHomeUser>();
		if (null != searchResult) {
			Iterator<Resource> resultResources = searchResult != null ? searchResult.getResources() : null;
		
			while (resultResources.hasNext()) {
				DamHomeUser damHomeUser = new DamHomeUser();
				Resource resource = resultResources.next();
				if (null != resource) {

					ValueMap properties = ResourceUtil.getValueMap(resource);
					String verb = properties.get("verb").toString();
					if (verb.equalsIgnoreCase("DOWNLOADED")) {
						// String createdBy = properties.get("jcr:createdBy").toString();
						damHomeUser.setUsrId(properties.get("jcr:createdBy").toString());
						if (resource.hasChildren()) {
							Iterator<Resource> resIterator = resource.getChildren().iterator();
							if (null != resIterator) {
								Resource childResource = resIterator.next();
								if (null != childResource.getPath() && childResource.getPath().contains("object")) {
									ValueMap childProp = ResourceUtil.getValueMap(childResource);
									String assetPath = childProp.get("link").toString();
									damHomeUser.setAssetPath(childProp.get("link").toString());
									LOGGER.debug("Asset Path is {}", assetPath);
								}
							}
						}

					}

				}
				damHomeUserList.add(damHomeUser);
			}
			
		}
		return damHomeUserList;
	}
 public void getExpiredDownloadAssetList(List expiredAsset, List<DamHomeUser> downloadAssets){
	 
 }
 
 */
}
