package com.bus.mvc.portlet;

import com.bus.mvc.constants.BusPortletKeys;
import com.bus.service.model.Bus;
import com.bus.service.service.BusLocalServiceUtil;
import com.liferay.portal.kernel.dao.orm.Criterion;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletException;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.osgi.service.component.annotations.Component;

/**
 * @author parameshwaran
 */
@Component(immediate = true, property = { "com.liferay.portlet.display-category=category.sample",
		"com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=Bus", "javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=" + BusPortletKeys.BUS,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user" }, service = Portlet.class)
public class BusPortlet extends MVCPortlet {

	/*
	 * @Override public void render(RenderRequest renderRequest, RenderResponse
	 * renderResponse) throws IOException, PortletException { List<Bus> busList =
	 * BusLocalServiceUtil.getBuses(-1, -1); String busId =
	 * ParamUtil.getString(renderRequest, "busId");
	 * renderRequest.setAttribute("busId", busId); if (busId != null) { Bus bus =
	 * null; try { bus = BusLocalServiceUtil.getBus(Long.valueOf(busId)); } catch
	 * (NumberFormatException | PortalException e) {
	 * 
	 * } renderRequest.setAttribute("bus", bus); }
	 * 
	 * renderRequest.setAttribute("busList", busList); super.render(renderRequest,
	 * renderResponse); }
	 */

	@Override
	public void render(RenderRequest renderRequest, RenderResponse renderResponse)
			throws IOException, PortletException {

		long busId = ParamUtil.getLong(renderRequest, "busId");
		if (busId > 0) {
			Bus bus = BusLocalServiceUtil.fetchBus(busId);
			renderRequest.setAttribute("bus", bus);
		}

		List<Bus> busList = (List<Bus>) renderRequest.getAttribute("busList");
		if (Validator.isNull(renderRequest.getAttribute("busList"))) {
			busList = BusLocalServiceUtil.getBuses(-1, -1);
		}

		renderRequest.setAttribute("total", busList.size());
		renderRequest.setAttribute("busList", busList);

		super.render(renderRequest, renderResponse);
	}

	@ProcessAction(name = "addbus")
	public void addbus(ActionRequest actionRequest, ActionResponse actionResponse) throws PortalException {
		long busId = ParamUtil.getLong(actionRequest, "busId");
		String busName = ParamUtil.getString(actionRequest, "busName");
		String busCompany = ParamUtil.getString(actionRequest, "busCompany");
		long busPrice = ParamUtil.getLong(actionRequest, "busPrice");

		Bus bus = null;
		if (busId > 0) {
			bus = BusLocalServiceUtil.getBus(busId);
			bus.setBusName(busName);
			bus.setBusPrice(busPrice);
			bus.setBusCompany(busCompany);
			BusLocalServiceUtil.updateBus(bus);

		} else {
			bus = BusLocalServiceUtil.createBus(0);
			bus.setBusName(busName);
			bus.setBusPrice(busPrice);
			bus.setBusCompany(busCompany);
			BusLocalServiceUtil.addBus(bus);
		}

	}

	@ProcessAction(name = "partySearch")
	public void partySearch(ActionRequest actionRequest, ActionResponse actionResponse) {

		System.out.println("partySearch");
		String keywords = ParamUtil.getString(actionRequest, "keywords");
		long busId = ParamUtil.getLong(actionRequest, "busId");
		String busName = ParamUtil.getString(actionRequest, "busName");
		String busCompany = ParamUtil.getString(actionRequest, "busCompany");
		long busPrice = ParamUtil.getLong(actionRequest, "busPrice");
		System.out.println("keywords:::" + keywords);
		System.out.println("busId:::" + busId);
		System.out.println("busName:::" + busName);
		System.out.println("busCompany:::" + busCompany);
		System.out.println("busPrice:::" + busPrice);
		List<Bus> busList = new ArrayList<Bus>();
		ClassLoader classLoader = getClass().getClassLoader();
		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(Bus.class, classLoader);
		Criterion criterion = null;
		if (keywords.equalsIgnoreCase("") && busCompany.equalsIgnoreCase("") && (busPrice == 0)) {
			criterion = RestrictionsFactoryUtil.like("busName", "%");
		} else {
			if (keywords != "") {
				criterion = RestrictionsFactoryUtil.like("busName", "%" + keywords + "%");
			}
			if (busPrice > 0) {
				if (keywords == "") {
					criterion = RestrictionsFactoryUtil.eq("busPrice", busPrice);
				} else {
					criterion = RestrictionsFactoryUtil.or(criterion, RestrictionsFactoryUtil.eq("busPrice", busPrice));
				}

			}
			if (busCompany != "" && Validator.isNotNull(busCompany)) {
				if (keywords == "" && busPrice == 0) {
					criterion = RestrictionsFactoryUtil.like("busCompany", "%" + busCompany + "%");
				} else {
					criterion = RestrictionsFactoryUtil.or(criterion,
							RestrictionsFactoryUtil.like("busCompany", "%" + busCompany + "%"));
				}
			}

		}

		dynamicQuery.add(criterion);
		busList = BusLocalServiceUtil.dynamicQuery(dynamicQuery);

		actionRequest.setAttribute("busList", busList);
	}

	@ProcessAction(name = "deletebus")
	public void deletebus(ActionRequest actionRequest, ActionResponse actionResponse) throws PortalException {
		long busId = ParamUtil.getLong(actionRequest, "busId");
		BusLocalServiceUtil.deleteBus(busId);
	}
}