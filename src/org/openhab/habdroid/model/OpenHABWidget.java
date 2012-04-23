/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2012, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */

package org.openhab.habdroid.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

/**
 * This is a class to hold basic information about openHAB widget.
 * 
 * @author Victor Belov
 *
 */

public class OpenHABWidget {
	private String label;
	private String icon;
	private String type;
	private String url;
	private OpenHABWidget parent;
	private OpenHABItem item;
	private OpenHABLinkedPage linkedPage;
	private ArrayList<OpenHABWidget> children;
	private ArrayList<OpenHABWidgetMapping> mappings;
	
	public OpenHABWidget() {
		this.children = new ArrayList<OpenHABWidget>();
		this.mappings = new ArrayList<OpenHABWidgetMapping>();
	}
	
	public OpenHABWidget(OpenHABWidget parent, Node startNode) {
		this.parent = parent;
		this.children = new ArrayList<OpenHABWidget>();
		this.mappings = new ArrayList<OpenHABWidgetMapping>();
		if (startNode.hasChildNodes()) {
			NodeList childNodes = startNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i ++) {
				Node childNode = childNodes.item(i);
				if (childNode.getNodeName().equals("item")) {
					this.setItem(new OpenHABItem(childNode));
				} else if (childNode.getNodeName().equals("linkedPage")) {					
					this.setLinkedPage(new OpenHABLinkedPage(childNode));
				} else if (childNode.getNodeName().equals("widget")) {
					new OpenHABWidget(this, childNode);
				} else {
					if (childNode.getNodeName().equals("type")) {
						this.type = childNode.getTextContent();
					} else if (childNode.getNodeName().equals("label")) {
						this.setLabel(childNode.getTextContent());
					} else if (childNode.getNodeName().equals("icon")) {
						this.setIcon(childNode.getTextContent());
					} else if (childNode.getNodeName().equals("url")) {
						this.setUrl(childNode.getTextContent());
					} else if (childNode.getNodeName().equals("mapping")) {
						NodeList mappingChildNodes = childNode.getChildNodes();
						String mappingCommand = "";
						String mappingLabel = "";
						for (int k = 0; k < mappingChildNodes.getLength(); k++) {
							if (mappingChildNodes.item(k).getNodeName().equals("command"))
								mappingCommand = mappingChildNodes.item(k).getTextContent();
							if (mappingChildNodes.item(k).getNodeName().equals("label"))
								mappingLabel = mappingChildNodes.item(k).getTextContent();
						}
						OpenHABWidgetMapping mapping = new OpenHABWidgetMapping(mappingCommand, mappingLabel);
						mappings.add(mapping);
					}
				}
			}
		}
		this.parent.addChildWidget(this);
	}
	
	public void addChildWidget(OpenHABWidget child) {
		if (child != null) {
			this.children.add(child);
		}
	}

	public boolean hasChildren() {
		if (this.children.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public ArrayList<OpenHABWidget> getChildren() {
		return this.children;
	}
	
	public boolean hasItem() {
		if (this.getItem() != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean hasLinkedPage() {
		if (this.getLinkedPage() != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public OpenHABItem getItem() {
		return item;
	}

	public void setItem(OpenHABItem item) {
		this.item = item;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public OpenHABLinkedPage getLinkedPage() {
		return linkedPage;
	}

	public void setLinkedPage(OpenHABLinkedPage linkedPage) {
		this.linkedPage = linkedPage;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public boolean hasMappings() {
		if (mappings.size() > 0) {
			return true;
		}
		return false;
	}
	
	public OpenHABWidgetMapping getMapping(int index) {
		return mappings.get(index);
	}
	
	public ArrayList<OpenHABWidgetMapping> getMappings() {
		return mappings;
	}
	
}