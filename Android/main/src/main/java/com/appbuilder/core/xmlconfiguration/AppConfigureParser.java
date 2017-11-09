/****************************************************************************
 *                                                                           *
 *  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
 *                                                                           *
 *  This file is part of iBuildApp.                                          *
 *                                                                           *
 *  This Source Code Form is subject to the terms of the iBuildApp License.  *
 *  You can obtain one at http://ibuildapp.com/license/                      *
 *                                                                           *
 ****************************************************************************/
package com.appbuilder.core.xmlconfiguration;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.util.Xml;
import com.appbuilder.core.GPSNotification.GPSItem;
import com.appbuilder.core.config.ConfigDBHelper;
import com.appbuilder.sdk.android.AppAdvData;
import com.appbuilder.sdk.android.BarDesigner;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.CharArrayReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


/**
 * Class responsible for parsing *.xml file
 * Input - xml string
 * Output - AppConfigure object
 * !!!!!!!!!!!!!!!!!!!!!!!!
 * Really used only one function of SAX parsing - parseSAX()
 * <p/>
 * <p/>
 * HOW TO USE
 * 1) Create object using AppConfigureParser() specifying input XML string
 * 2) Call function parseSAX()
 * 3) Get result AppConfigure object for further using
 */
public class AppConfigureParser {

    private InputStream xmlStream;
    private String xml = "";
    private AppConfigure appConfig = new AppConfigure();
    private final Context ctx;

    public AppConfigureParser(Context ctx, String xml) {
        this.xml = xml;
        this.ctx = ctx;
    }

    public AppConfigureParser(Context ctx, InputStream xml) {
        this.xmlStream = xml;
        this.ctx = ctx;
    }

    private static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    private String removeSpec(String source) {
        return source.replace("&amp;", "&").replace("&apos;", "\'").replace("&quot;", "").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&nbsp;", " ");
    }

    private boolean checkForSpec(String source) {
        if (source.contains("&amp;"))
            return true;
        else if (source.contains("&apos;"))
            return true;
        else if (source.contains("&quot;"))
            return true;
        else if (source.contains("&lt;"))
            return true;
        else if (source.contains("&gt;"))
            return true;
        else
            return false;
    }

    public AppConfigure parse() throws RuntimeException {

        if (xml.length() == 0) {
            if (xmlStream == null) {
                return appConfig;
            }
        }
        Document document;

        try {
            CharArrayReader reader = null;

            if (xml.length() == 0) {
                reader = new CharArrayReader(xml.toCharArray());
            } else {

            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setCoalescing(true); // for CDATA
            DocumentBuilder builder = factory.newDocumentBuilder();

            if (xml.length() != 0) {
                document = builder.parse(new InputSource(reader));
            } else {
                document = builder.parse(new InputSource(xmlStream));
            }
            NodeList appNameList = document.getElementsByTagName("appName");
            Element appNameElement = (Element) appNameList.item(0);
            String appName = getCharacterDataFromElement(appNameElement);
            appConfig.setAppName(appName);
            Log.d("parse", "AppName = " + appName);

            NodeList rgbcolorList = document.getElementsByTagName("rgbcolor");
            NodeList findrgbcolorList = rgbcolorList.item(0).getChildNodes();
            String rgbcolor = (findrgbcolorList.item(0).getNodeValue());
            appConfig.setBackgroundColor(rgbcolor);
            Log.d("parse", "App color = " + rgbcolor);

            NodeList backimageList = document.getElementsByTagName("backimage");
            Element backimageElement = (Element) backimageList.item(0);
            String backimage = getCharacterDataFromElement(backimageElement);
            appConfig.setBackgroundImageUrl(backimage);
            Log.d("parse", "App backimage Url = " + backimage);

            try {
                NodeList backimageDataList = document.getElementsByTagName("backimagedata");
                Element backimageDataElement = (Element) backimageDataList.item(0);
                String backimageData = getCharacterDataFromElement(backimageDataElement);
                appConfig.setmBackgorundImageData(backimageData);
                Log.d("parse", "App backimage src= " + backimageData);
            } catch (NullPointerException nPEx) {
                //Log.e("", nPEx.getMessage());
            }

            NodeList showLinkList = document.getElementsByTagName("showLink");
            NodeList findShowLinkList = showLinkList.item(0).getChildNodes();
            int showLink = Integer.parseInt((findShowLinkList.item(0).getNodeValue()));
            appConfig.setShowLink(showLink > 0);
            Log.d("parse", "App showLink = " + /*Boolean.toString(showLink) */ String.valueOf(showLink));

            try {
                NodeList dateFormatList = document.getElementsByTagName("dateformat");
                NodeList findDateFormatList = dateFormatList.item(0).getChildNodes();
                int dateFormat = Integer.parseInt((findDateFormatList.item(0).getNodeValue()));
                appConfig.setDateFormat(dateFormat);
                Log.d("parse", "App dateFormat = " + /*Boolean.toString(showLink) */ String.valueOf(dateFormat));
            } catch (Exception e) {
                Log.d("parse", "App dateFormat (not exists) = 0");
                appConfig.setDateFormat(0);
            }


            try {
                NodeList showMenuList = document.getElementsByTagName("showMenu");
                NodeList findShowMenuList = showMenuList.item(0).getChildNodes();
                //Log.d("parse", "ShowLink value = " + findShowLinkList.item(0).getNodeValue());
                //boolean showLink = Boolean.parseBoolean( (findShowLinkList.item(0).getNodeValue()) );
                int showMenu = Integer.parseInt((findShowMenuList.item(0).getNodeValue()));
                appConfig.setShowMenu(showMenu);
                Log.d("parse", "App showMenu = " + /*Boolean.toString(showLink) */ String.valueOf(showMenu));
            } catch (Exception e) {
                Log.d("parse", "App showMenu (not exists) = 0");
                appConfig.setShowMenu(0);
            }

            NodeList nodesImages = document.getElementsByTagName("image");
            int nImagesCount = nodesImages.getLength();
            for (int i = 0; i < nImagesCount; i++) {
                WidgetUIImage image = new WidgetUIImage();
                Node node = nodesImages.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                int x;
                NodeList xCoordList = ((Element) node).getElementsByTagName("x");
                NodeList findXCoordList = xCoordList.item(0).getChildNodes();
                x = Integer.parseInt((findXCoordList.item(0).getNodeValue()));
                Log.d("parse", "Image x = " + Integer.toString(x));
                image.setLeft(x);

                int y;
                NodeList yCoordList = ((Element) node).getElementsByTagName("y");
                NodeList findYCoordList = yCoordList.item(0).getChildNodes();
                y = Integer.parseInt((findYCoordList.item(0).getNodeValue()));
                Log.d("parse", "Image y = " + Integer.toString(y));
                image.setTop(y);

                int width;
                NodeList widthList = ((Element) node).getElementsByTagName("width");
                NodeList findWidthList = widthList.item(0).getChildNodes();
                width = Integer.parseInt((findWidthList.item(0).getNodeValue()));
                Log.d("parse", "Image width = " + Integer.toString(width));
                image.setWidth(width);

                int height;
                NodeList heightList = ((Element) node).getElementsByTagName("height");
                NodeList findHeightList = heightList.item(0).getChildNodes();
                height = Integer.parseInt((findHeightList.item(0).getNodeValue()));
                Log.d("parse", "Image height = " + Integer.toString(height));
                image.setHeight(height);

                NodeList iconList = ((Element) node).getElementsByTagName("url");
                Element iconElement = (Element) iconList.item(0);
                String iconUrl = getCharacterDataFromElement(iconElement);
                image.setSourceUrl(iconUrl);
                Log.d("parse", "Image source url = " + iconUrl);

                try {
                    NodeList dataList = ((Element) node).getElementsByTagName("imagedata");
                    Element dataElement = (Element) dataList.item(0);
                    String imagedata = getCharacterDataFromElement(dataElement);
                    image.setmImageData(imagedata);
                    Log.d("parse", "Image source = " + imagedata);
                } catch (NullPointerException nPEx) {
                    //        Log.e("", nPEx.getMessage());
                }

                appConfig.addImage(image);
            }

			
			/* push notification */
            try {
                NodeList nodesPushNs = document.getElementsByTagName("pushns");
                if (nodesPushNs != null) {
                    for (int i = 0; i < nodesPushNs.getLength(); i++) {
                        Node node = nodesPushNs.item(i);
                        if (node != null) {
                            if (node.getNodeType() != Node.ELEMENT_NODE) {
                                continue;
                            }
                            if (!"app".equalsIgnoreCase(node.getParentNode().getNodeName())) {
                                continue;
                            }
                            String pushNotificationAccount = "";
                            NodeList typeList = ((Element) node).getElementsByTagName("android_account");
                            if (typeList.item(0) != null) {
                                NodeList findTypeList = typeList.item(0).getChildNodes();
                                pushNotificationAccount = findTypeList.item(0).getNodeValue();
                            }
                            appConfig.setPushNotificationAccount(pushNotificationAccount);
                        }
                    }
                }
            } catch (Exception e) {
                //          Log.w("", e.getMessage());
            }

			
			/* gps notifications*/
            NodeList nodesGPSNs = document.getElementsByTagName("gps_object");
            if (nodesGPSNs != null) {
                for (int i = 0; i < nodesGPSNs.getLength(); i++) {
                    try {
                        Node node = nodesGPSNs.item(i);
                        if (node != null) {
                            if (node.getNodeType() != Node.ELEMENT_NODE)
                                continue;

                            if (!"app".equalsIgnoreCase(node.getParentNode().getNodeName()))
                                continue;

                            String value = "";
                            NodeList nodeList;

                            GPSItem gpsItem = new GPSItem();
                            nodeList = ((Element) node).getElementsByTagName("latitude");
                            if (nodeList.item(0) != null) {
                                NodeList findTypeList = nodeList.item(0).getChildNodes();
                                value = findTypeList.item(0).getNodeValue();
                            }
                            gpsItem.setLatitude(new Double(value).doubleValue());

                            nodeList = ((Element) node).getElementsByTagName("longitude");
                            if (nodeList.item(0) != null) {
                                NodeList findTypeList = nodeList.item(0).getChildNodes();
                                value = findTypeList.item(0).getNodeValue();
                            }
                            gpsItem.setLongitude(new Double(value).doubleValue());

                            nodeList = ((Element) node).getElementsByTagName("title");
                            if (nodeList.item(0) != null) {
                                NodeList findTypeList = nodeList.item(0).getChildNodes();
                                value = findTypeList.item(0).getNodeValue();
                            }
                            gpsItem.setTitle(value);

                            nodeList = ((Element) node).getElementsByTagName("description");
                            if (nodeList.item(0) != null) {
                                NodeList findTypeList = nodeList.item(0).getChildNodes();
                                value = findTypeList.item(0).getNodeValue();
                            }
                            gpsItem.setDescription(value);

                            nodeList = ((Element) node).getElementsByTagName("radius");
                            if (nodeList.item(0) != null) {
                                NodeList findTypeList = nodeList.item(0).getChildNodes();
                                value = findTypeList.item(0).getNodeValue();
                            }
                            gpsItem.setRadius(new Integer(value).intValue());

                            nodeList = ((Element) node).getElementsByTagName("show");
                            if (nodeList.item(0) != null) {
                                NodeList findTypeList = nodeList.item(0).getChildNodes();
                                value = findTypeList.item(0).getNodeValue();
                            }
                            if (value.equals("plural"))
                                gpsItem.setCountOfView(GPSItem.Show.PLURAL);

                            appConfig.addGPSNotification(gpsItem);
                        }
                    } catch (Exception e) {
//                                            Log.w("", e.getMessage());
                    }
                }
            }

			
			/* ads */
            AppAdvData appAdv = new AppAdvData();
            NodeList nodesAdv = document.getElementsByTagName("adv");
            int nAdvCount = nodesAdv.getLength();
            for (int i = 0; i < nAdvCount; i++) {
                Node node = nodesAdv.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                if (!"app".equalsIgnoreCase(node.getParentNode().getNodeName())) {
                    continue;
                }

                String advType = "";
                NodeList typeList = ((Element) node).getElementsByTagName("type");
                if (typeList.item(0) != null) {
                    NodeList findTypeList = typeList.item(0).getChildNodes();
                    advType = findTypeList.item(0).getNodeValue();
                }
                if (appAdv.getAdvType().length() == 0) {
                    appAdv.setAdvType(advType);
                }
                Log.d("parse", "Adv type = " + advType);

                String advRedirect = "";
                NodeList urlRedirectList = ((Element) node).getElementsByTagName("url_on_click");
                if (urlRedirectList.item(0) != null) {
                    NodeList findRedirectList = urlRedirectList.item(0).getChildNodes();
                    advRedirect = findRedirectList.item(0).getNodeValue();
                }
                appAdv.setAdvRedirect(advRedirect);
                Log.d("parse", "Adv redirect = " + advRedirect);

                String advContent = "";
                NodeList urlList = ((Element) node).getElementsByTagName("url");
                if (urlList.item(0) != null) {
                    NodeList findUrlList = urlList.item(0).getChildNodes();
                    advContent = findUrlList.item(0).getNodeValue();
                    appAdv.setAdvType("url");
                }
                appAdv.setAdvContent(advContent);
                Log.d("parse", "Adv content Url = " + advContent);

                try {
                    NodeList htmlRedirectList = ((Element) node).getElementsByTagName("html_on_click");
                    if (htmlRedirectList.item(0) != null) {
                        NodeList findRedirectList = htmlRedirectList.item(0).getChildNodes();
                        advRedirect = findRedirectList.item(0).getNodeValue();
                    }
                    appAdv.setAdvRedirect(advRedirect);
                } catch (Exception ex) {
                    Log.e("", "");
                }
                Log.d("parse", "Adv redirect = " + advRedirect);

                NodeList htmlList = ((Element) node).getElementsByTagName("html");
                if (htmlList.item(0) != null) {
                    NodeList findHtmlList = htmlList.item(0).getChildNodes();
                    advContent = findHtmlList.item(0).getNodeValue();
                    appAdv.setAdvType("html");
                }
                appAdv.setAdvContent(advContent);
                Log.d("parse", "Adv content Html = " + advContent);

                NodeList pubIdList = ((Element) node).getElementsByTagName("uid");
                if (pubIdList.item(0) != null) {
                    NodeList findPubIdList = pubIdList.item(0).getChildNodes();
                    advContent = findPubIdList.item(0).getNodeValue();
                }
                appAdv.setAdvContent(advContent);
                Log.d("parse", "Adv content Html = " + advContent);
            }
            appConfig.setAppAdv(appAdv);

			/* labels */
            NodeList nodesLabels = document.getElementsByTagName("label");
            int nLabelsCount = nodesLabels.getLength();
            for (int i = 0; i < nLabelsCount; i++) {
                WidgetUILabel label = new WidgetUILabel();
                Node node = nodesLabels.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                //if (node.getParentNode().getNodeName() != "app") {
                if (!"app".equalsIgnoreCase(node.getParentNode().getNodeName())) {
                    continue;
                }

                int x;
                NodeList xCoordList = ((Element) node).getElementsByTagName("x");
                NodeList findXCoordList = xCoordList.item(0).getChildNodes();
                x = Integer.parseInt((findXCoordList.item(0).getNodeValue()));
                Log.d("parse", "Label x = " + Integer.toString(x));
                label.setLeft(x);

                int y;
                NodeList yCoordList = ((Element) node).getElementsByTagName("y");
                NodeList findYCoordList = yCoordList.item(0).getChildNodes();
                y = Integer.parseInt((findYCoordList.item(0).getNodeValue()));
                Log.d("parse", "Label y = " + Integer.toString(y));
                label.setTop(y);

                int width;
                NodeList widthList = ((Element) node).getElementsByTagName("width");
                NodeList findWidthList = widthList.item(0).getChildNodes();
                width = Integer.parseInt((findWidthList.item(0).getNodeValue()));
                Log.d("parse", "Label width = " + Integer.toString(width));
                label.setWidth(width);

                int height;
                NodeList heightList = ((Element) node).getElementsByTagName("height");
                NodeList findHeightList = heightList.item(0).getChildNodes();
                height = Integer.parseInt((findHeightList.item(0).getNodeValue()));
                Log.d("parse", "Label height = " + Integer.toString(height));
                label.setHeight(height);

                NodeList titleList = ((Element) node).getElementsByTagName("title");
                Element titleElement = (Element) titleList.item(0);
                String title = getCharacterDataFromElement(titleElement);
                label.setTitle(title);
                Log.d("parse", "Label title = " + title);

                try {
                    NodeList sizeList = ((Element) node).getElementsByTagName("size");
                    NodeList findSizeList = sizeList.item(0).getChildNodes();
                    int size = Integer.parseInt((findSizeList.item(0).getNodeValue()));
                    label.setFontSize(size);
                    Log.d("parse", "Label font size = " + Integer.toString(size));

                    NodeList colorList = ((Element) node).getElementsByTagName("color");
                    NodeList findColorList = colorList.item(0).getChildNodes();
                    String color = (findColorList.item(0).getNodeValue());
                    label.setColor(color);
                    Log.d("parse", "Label color = " + color);

                    NodeList styleList = ((Element) node).getElementsByTagName("style");
                    NodeList findStyleList = styleList.item(0).getChildNodes();
                    String style = (findStyleList.item(0).getNodeValue());
                    label.setStyle(style);
                    Log.d("parse", "Label style = " + style);
                } catch (Exception e) {
                    //      Log.w("", e.getMessage());
                }

                appConfig.addLabel(label);
            }

            NodeList nodesButtons = document.getElementsByTagName("button");
            int nButtonsCount = nodesButtons.getLength();
            for (int i = 0; i < nButtonsCount; i++) {
                WidgetUIButton button = new WidgetUIButton();
                Node node = nodesButtons.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                int x;
                NodeList xCoordList = ((Element) node).getElementsByTagName("x");
                NodeList findXCoordList = xCoordList.item(0).getChildNodes();
                x = Integer.parseInt((findXCoordList.item(0).getNodeValue()));
                Log.d("parse", "Button x = " + Integer.toString(x));
                button.setLeft(x);

                int y;
                NodeList yCoordList = ((Element) node).getElementsByTagName("y");
                NodeList findYCoordList = yCoordList.item(0).getChildNodes();
                y = Integer.parseInt((findYCoordList.item(0).getNodeValue()));
                Log.d("parse", "Button y = " + Integer.toString(y));
                button.setTop(y);

                int width;
                NodeList widthList = ((Element) node).getElementsByTagName("width");
                NodeList findWidthList = widthList.item(0).getChildNodes();
                width = Integer.parseInt((findWidthList.item(0).getNodeValue()));
                Log.d("parse", "Button width = " + Integer.toString(width));
                button.setWidth(width);

                int height;
                NodeList heightList = ((Element) node).getElementsByTagName("height");
                NodeList findHeightList = heightList.item(0).getChildNodes();
                height = Integer.parseInt((findHeightList.item(0).getNodeValue()));
                Log.d("parse", "Button height = " + Integer.toString(height));
                button.setHeight(height);

                String iconUrl = null;
                try {
                    NodeList iconList = ((Element) node).getElementsByTagName("icon");
                    Element iconElement = (Element) iconList.item(0);
                    iconUrl = getCharacterDataFromElement(iconElement);
                    button.setImageSourceUrl(iconUrl);
                } catch (NullPointerException e) {
                    button.setImageSourceUrl("");
                }
                Log.d("parse", "Button icon url = " + iconUrl);

                String iconData;
                try {
                    NodeList dataList = ((Element) node).getElementsByTagName("icondata");
                    Element iconElement = (Element) dataList.item(0);
                    iconData = getCharacterDataFromElement(iconElement);
                    button.setmImageData(iconData);
                } catch (NullPointerException e) {
                    button.setmImageData("");
                }
                Log.d("parse", "Button icon url = " + iconUrl);

                NodeList titleList = ((Element) node).getElementsByTagName("label");
                Element titleElement = (Element) titleList.item(0);
                String title = getCharacterDataFromElement(titleElement);
                button.setTitle(title);
                Log.d("parse", "Button title = " + title);

                NodeList sizeList = ((Element) node).getElementsByTagName("size");
                NodeList findSizeList = sizeList.item(0).getChildNodes();
                int size = Integer.parseInt((findSizeList.item(0).getNodeValue()));
                button.setFontSize(size);
                Log.d("parse", "Button font size = " + Integer.toString(size));

                NodeList alignList = ((Element) node).getElementsByTagName("align");
                NodeList findAlignList = alignList.item(0).getChildNodes();
                String align = (findAlignList.item(0).getNodeValue());
                button.setAlign(align);
                Log.d("parse", "Button align = " + align);

                NodeList colorList = ((Element) node).getElementsByTagName("color");
                NodeList findColorList = colorList.item(0).getChildNodes();
                String color = (findColorList.item(0).getNodeValue());
                button.setColor(color);
                Log.d("parse", "Button color = " + color);

                NodeList styleList = ((Element) node).getElementsByTagName("style");
                NodeList findStyleList = styleList.item(0).getChildNodes();
                String style = null;
                try {
                    style = (findStyleList.item(0).getNodeValue());
                    button.setStyle(style);
                } catch (Exception e) {
                    button.setStyle("");
                }
                Log.d("parse", "Button style = " + style);

                NodeList funcList = ((Element) node).getElementsByTagName("func");
                NodeList findFuncList = funcList.item(0).getChildNodes();
                int func = Integer.parseInt((findFuncList.item(0).getNodeValue()));
                button.setOrder(func);
                Log.d("parse", "Button func = " + Integer.toString(func));

                appConfig.addButton(button);
            }

            NodeList nodesTabs = document.getElementsByTagName("tabItem");
            int nTabsCount = nodesTabs.getLength();
            for (int i = 0; i < nTabsCount; i++) {
                WidgetUITab tab = new WidgetUITab();
                Node node = nodesTabs.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }


                String iconUrl = null;
                try {
                    NodeList iconList = ((Element) node).getElementsByTagName("icon");
                    Element iconElement = (Element) iconList.item(0);
                    iconUrl = getCharacterDataFromElement(iconElement);
                    tab.setIconUrl(iconUrl);
                } catch (NullPointerException e) {
                    tab.setIconUrl("");
                }
                Log.d("parse", "Tab icon url = " + iconUrl);

                String iconData;
                try {
                    NodeList iconDataList = ((Element) node).getElementsByTagName("icondata");
                    Element iconDataElement = (Element) iconDataList.item(0);
                    iconData = getCharacterDataFromElement(iconDataElement);
                    tab.setmIconData(iconData);
                } catch (NullPointerException e) {
                    tab.setmIconData("");
                }
                Log.d("parse", "Tab icon url = " + iconUrl);

                NodeList labelList = ((Element) node).getElementsByTagName("label");
                Element labelElement = (Element) labelList.item(0);
                String label = getCharacterDataFromElement(labelElement);
                tab.setLabel(label);
                Log.d("parse", "Tab title = " + label);

                NodeList funcList = ((Element) node).getElementsByTagName("func");
                NodeList findFuncList = funcList.item(0).getChildNodes();
                int func = Integer.parseInt((findFuncList.item(0).getNodeValue()));
                tab.setOrder(func);
                Log.d("parse", "Tab func = " + Integer.toString(func));

                appConfig.addTab(tab);
            }

			/* parse widgets supper power */
            HashMap<String, ArrayList<String>> plugins = new HashMap<String, ArrayList<String>>();
            NodeList nodesPlugins = document.getElementsByTagName("plugin");
            if (nodesPlugins != null) {
                for (int i = 0; i < nodesPlugins.getLength(); i++) {
                    Node node = nodesPlugins.item(i);
                    if (node.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }
                    try {
                        NodeList funcList = ((Element) node).getElementsByTagName("func");
                        NodeList findFuncList = funcList.item(0).getChildNodes();
                        String func = (findFuncList.item(0).getNodeValue());

                        NodeList typeList = ((Element) node).getElementsByTagName("type");
                        NodeList findTypeList = typeList.item(0).getChildNodes();
                        String type = (findTypeList.item(0).getNodeValue());

                        if (plugins.containsKey(func)) {
                            plugins.get(func).add(type);
                        } else {
                            plugins.put(func, new ArrayList<String>());
                            plugins.get(func).add(type);
                        }
                    } catch (Exception e) {
                        //            Log.e("", e.getMessage());
                    }
                }
            }

            NodeList nodesWidgets = document.getElementsByTagName("widget");
            int nWidgetsCount = nodesWidgets.getLength();
            for (int i = 0; i < nWidgetsCount; i++) {
                Widget widget = new Widget();
                widget.setAppName(appConfig.getAppName());
                widget.setDateFormat(appConfig.getDateFormat());

                Node node = nodesWidgets.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                try {
                    NodeList titleList = ((Element) node).getElementsByTagName("title");
                    NodeList findTitleList = titleList.item(0).getChildNodes();
                    String title = (findTitleList.item(0).getNodeValue());
                    widget.setTitle(title);
                    Log.d("parse", "Widget title = " + title);
                } catch (Exception e) {
                    //        Log.e("", e.getMessage());
                }

                String order = "";
                try {
                    NodeList orderList = ((Element) node).getElementsByTagName("order");
                    NodeList findOrderList = orderList.item(0).getChildNodes();
                    order = (findOrderList.item(0).getNodeValue());
                    widget.setOrder(Integer.parseInt(order));
                    Log.d("parse", "Widget order = " + order);
                } catch (Exception e) {
                    //       Log.e("", e.getMessage());
                }

                try {
                    NodeList nameList = ((Element) node).getElementsByTagName("name");
                    NodeList findNameList = nameList.item(0).getChildNodes();
                    String pluginName = (findNameList.item(0).getNodeValue());
                    widget.setPluginName(pluginName);
                    Log.d("parse", "Widget plugin name = " + pluginName);
                } catch (Exception e) {
                    //       Log.e("", e.getMessage());
                }

                try {
                    NodeList nameList = ((Element) node).getElementsByTagName("type");
                    NodeList findNameList = nameList.item(0).getChildNodes();
                    String pluginType = (findNameList.item(0).getNodeValue());
                    widget.setPluginType(pluginType);
                    //Log.d("parse", "Widget plugin name = " + pluginName);
                } catch (Exception e) {
                    //       Log.e("", e.getMessage());
                }

                try {
                    NodeList packageList = ((Element) node).getElementsByTagName("package");
                    NodeList findPackageList = packageList.item(0).getChildNodes();
                    String pluginPackage = (findPackageList.item(0).getNodeValue());
                    widget.setPluginPackage(pluginPackage);
                    Log.d("parse", "Widget plugin package = " + pluginPackage);
                } catch (Exception e) {
                    //         Log.e("", e.getMessage());
                }

                try {
                    NodeList hashList = ((Element) node).getElementsByTagName("hash");
                    NodeList findHashList = hashList.item(0).getChildNodes();
                    String pluginHash = (findHashList.item(0).getNodeValue());
                    widget.setPluginHash(pluginHash);
                    Log.d("parse", "Widget plugin hash = " + pluginHash);
                } catch (Exception e) {
                    //         Log.e("", e.getMessage());
                }

                try {
                    NodeList urlList = ((Element) node).getElementsByTagName("url");
                    NodeList findUrlList = urlList.item(0).getChildNodes();
                    String pluginUrl = (findUrlList.item(0).getNodeValue());
                    widget.setPluginUrl(pluginUrl);
                    Log.d("parse", "Widget plugin url = " + pluginUrl);
                } catch (Exception e) {
                    //         Log.e("", e.getMessage());
                }

                try {
                    NodeList backgroundColorList = ((Element) node).getElementsByTagName("background");
                    NodeList findBackgroundColorList = backgroundColorList.item(0).getChildNodes();
                    String backgroundColor = (findBackgroundColorList.item(0).getNodeValue());
                    widget.setBackground(backgroundColor);
                    Log.d("parse", "Widget background = " + backgroundColor);
                } catch (Exception e) {
                    //          Log.e("", e.getMessage());
                }

                try {
                    NodeList textColorList = ((Element) node).getElementsByTagName("textColor");
                    NodeList findTextColorList = textColorList.item(0).getChildNodes();
                    String textColor = (findTextColorList.item(0).getNodeValue());
                    widget.setTextColor(textColor);
                    Log.d("parse", "Widget text color = " + textColor);
                } catch (Exception e) {
                    //             Log.e("", e.getMessage());
                }

                try {
                    NodeList dataList = ((Element) node).getElementsByTagName("data");
                    Element pluginDataElement = (Element) dataList.item(0);
                    String pluginData = getCharacterDataFromElement(pluginDataElement);
                    widget.setPluginXmlData(pluginData);
                    Log.d("parse", "Widget plugin data = " + pluginData);
                } catch (Exception e) {
                    //            Log.e("", e.getMessage());
                }

				/* add to widget super power */
                if (plugins.containsKey(order)) {
                    ArrayList<String> params = plugins.get(order);
                    for (int p = 0; p < params.size(); p++) {
                        widget.addParameter(params.get(p), new Boolean(true));
                    }
                }
                appConfig.addWidget(widget);
            }

        } catch (Exception e) {
            //	Log.w("PARSE", e);
        }

        return appConfig;
    }


    /**
     *
     * */
    public AppConfigure parseSAX() {
        try {
            if (xml.length() > 0) {
                Xml.parse(xml, new AppConfigureHandler(ctx));
            } else {
                Xml.parse(xmlStream, Xml.Encoding.UTF_8, new AppConfigureHandler(ctx));
            }

            ConfigDBHelper.removeOldWidgets(ctx, appConfig.getmWidgets());
        } catch (Exception e) {
            Log.d("", "");
        }

        return appConfig;
    }

    private class AppConfigureHandler extends DefaultHandler {

        public AppConfigureHandler (Context ctx) {
            this.ctx = ctx;
        }

        private Context ctx;

        private boolean inPushNs = false;
        private boolean inAdv = false;
        private boolean inPlugin = false;

        private StringBuilder sb = new StringBuilder();

        //        private LoginScreen loginScreen = null;
        private WidgetUIImage image = null;
        private WidgetUILabel label = null;
        private WidgetUIButton button = null;
        private WidgetUITab tab = null;
        private WidgetUISidebarItem sidebarItem = null;
        private GPSItem gpsItem = null;
        private AppAdvData advData = new AppAdvData();
        private Widget widget = null;
        private BarDesigner designer = null;
        private String barName = "";
        private String barTitle = "";
        private String itemPosition = "";

        private String func = "";
        private String type = "";

        private LoginForm loginForm;

        HashMap<String, ArrayList<String>> plugins = new HashMap<String, ArrayList<String>>();

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            appConfig.setAppAdv(advData);
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();

            for (int i = 0; i < appConfig.getWidgetsCount(); i++) {
                Widget w = appConfig.getWidgetAtIndex(i);
                if (plugins.containsKey(new Integer(w.getOrder()).toString())) {
                    ArrayList<String> params = plugins.get(new Integer(w.getOrder()).toString());
                    for (int p = 0; p < params.size(); p++) {
                        w.addParameter(params.get(p), new Boolean(true));
                    }
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
//            if (localName.equalsIgnoreCase("loginScreen")) {
//                appConfig.setLoginScreen(new LoginScreen());
//            } else
            if (localName.equalsIgnoreCase("image")) {
                image = new WidgetUIImage();
            } else if (localName.equalsIgnoreCase("pushns")) {
                inPushNs = true;
            } else if (localName.equalsIgnoreCase("gps_object")) {
                gpsItem = new GPSItem();
            } else if (localName.equalsIgnoreCase("adv")) {
                inAdv = true;
            } else if (localName.equalsIgnoreCase("label")) {
                if (button == null && tab == null) {
                    label = new WidgetUILabel();
                }
            } else if (localName.equalsIgnoreCase("button")) {
                button = new WidgetUIButton();
            } else if (localName.equalsIgnoreCase("tabitem")) {
                tab = new WidgetUITab();
            } else if (localName.equalsIgnoreCase("plugin")) {
                inPlugin = true;
            } else if (localName.equalsIgnoreCase("sidebarItem")) {
                sidebarItem = new WidgetUISidebarItem();
            } else if (localName.equalsIgnoreCase("widget")) {
                widget = new Widget();
                widget.setAppName(appConfig.getAppName());
                widget.setDateFormat(appConfig.getDateFormat());
            } else if (localName.equalsIgnoreCase("navBar")) {
                designer = new BarDesigner();
                barName = "navBar";
                try {
                    designer.color = Color.parseColor(attributes.getValue("color"));
                } catch (Exception e) {
                }
            } else if (localName.equalsIgnoreCase("tabBar")) {
                designer = new BarDesigner();
                barName = "tabBar";
                try {
                    designer.color = Color.parseColor(attributes.getValue("color"));
                } catch (Exception e) {
                }
            } else if (localName.equalsIgnoreCase("bottomBar")) {
                designer = new BarDesigner();
                barName = "bottomBar";
                try {
                    designer.color = Color.parseColor(attributes.getValue("color"));
                } catch (Exception e) {
                }
            } else if (localName.equalsIgnoreCase("title")) {
                if (designer != null) {
                    barTitle = "title";
                    try {
                        designer.titleDesign.textColor = Color.parseColor(attributes.getValue("textColor").trim());
                    } catch (Exception e) {
                    }

                    try {
                        designer.titleDesign.selectedColor = Color.parseColor(attributes.getValue("selectionColor").trim());
                    } catch (Exception e) {
                    }

                    designer.titleDesign.textAlignment = attributes.getValue("textAlignment").trim();
                    designer.titleDesign.numberOfLines = Integer.parseInt(attributes.getValue("numberOfLines").trim());
                }
            } else if (localName.equalsIgnoreCase("item")) {
                if (designer != null) {
                    barTitle = "item";
                    itemPosition = attributes.getValue("position");

                    if (itemPosition == null) {
                        try {
                            try {
                                designer.itemDesign.textColor = Color.parseColor(attributes.getValue("textColor").trim());
                            } catch (Exception e) {
                            }

                            try {
                                designer.itemDesign.selectedColor = Color.parseColor(attributes.getValue("selectionColor").trim());
                            } catch (Exception e) {
                            }

                            designer.itemDesign.textAlignment = attributes.getValue("textAlignment").trim();
                            designer.itemDesign.numberOfLines = Integer.parseInt(attributes.getValue("numberOfLines").trim());
                        } catch (Exception e) {
                        }

                    } else if (itemPosition.compareTo("left") == 0) {
                        try {
                            try {
                                designer.leftButtonDesign.textColor = Color.parseColor(attributes.getValue("textColor").trim());
                            } catch (Exception e) {
                            }

                            try {
                                designer.leftButtonDesign.selectedColor = Color.parseColor(attributes.getValue("selectionColor").trim());
                            } catch (Exception e) {
                            }

                            designer.leftButtonDesign.textAlignment = attributes.getValue("textAlignment").trim();
                            designer.leftButtonDesign.numberOfLines = Integer.parseInt(attributes.getValue("numberOfLines").trim());
                        } catch (Exception e) {
                        }

                    } else if (itemPosition.compareTo("right") == 0) {
                        try {
                            try {
                                designer.rightButtonDesign.textColor = Color.parseColor(attributes.getValue("textColor").trim());
                            } catch (Exception e) {
                            }

                            try {
                                designer.rightButtonDesign.selectedColor = Color.parseColor(attributes.getValue("selectionColor").trim());
                            } catch (Exception e) {
                            }

                            designer.rightButtonDesign.textAlignment = attributes.getValue("textAlignment").trim();
                            designer.rightButtonDesign.numberOfLines = Integer.parseInt(attributes.getValue("numberOfLines").trim());
                        } catch (Exception e) {
                        }
                    }
                }
            } else if (localName.equalsIgnoreCase("font")) {
                if (designer != null) {
                    if (barTitle.compareToIgnoreCase("title") == 0) {
                        try {
                            designer.titleDesign.fontFamily = attributes.getValue("family").trim();
                            designer.titleDesign.fontSize = Integer.parseInt(attributes.getValue("size").trim());
                            designer.titleDesign.fontWeight = attributes.getValue("weight").trim();
                        } catch (Exception e) {
                        }
                    } else if (barTitle.compareToIgnoreCase("item") == 0) {
                        if (itemPosition == null) {
                            designer.itemDesign.fontFamily = attributes.getValue("family").trim();
                            designer.itemDesign.fontSize = Integer.parseInt(attributes.getValue("size").trim());
                            designer.itemDesign.fontWeight = attributes.getValue("weight").trim();
                        } else if (itemPosition.compareTo("left") == 0) {
                            designer.leftButtonDesign.fontFamily = attributes.getValue("family").trim();
                            designer.leftButtonDesign.fontSize = Integer.parseInt(attributes.getValue("size").trim());
                            designer.leftButtonDesign.fontWeight = attributes.getValue("weight").trim();
                        } else if (itemPosition.compareTo("right") == 0) {
                            designer.rightButtonDesign.fontFamily = attributes.getValue("family").trim();
                            designer.rightButtonDesign.fontSize = Integer.parseInt(attributes.getValue("size").trim());
                            designer.rightButtonDesign.fontWeight = attributes.getValue("weight").trim();
                        }
                    }
                    barTitle = "";
                }
            } else if(localName.equalsIgnoreCase("loginScreen")) {
                loginForm = new LoginForm();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            sb.append(new String(ch, start, length));
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (localName.equalsIgnoreCase("gaUserCode")) {
                appConfig.setGoogleAnalyticsId(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("appname")) {
                appConfig.setAppName(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("rgbcolor")) {
                appConfig.setBackgroundColor(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("backimage")) {
                appConfig.setBackgroundImageUrl(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("backimagedata")) {
                appConfig.setmBackgorundImageData(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("backimage_res")) {
                appConfig.setmBackgorundImageRes(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("showlink")) {
                int showLink = (int) (Float.parseFloat(sb.toString().trim()));
                appConfig.setShowLink(showLink > 0);
            } else if (localName.equalsIgnoreCase("autorun")) {
                try {
                    appConfig.setDefaultOrder(Integer.parseInt(sb.toString().trim()));
                } catch (Throwable thr) {
                    Log.d("", "");
                }
            } else if (localName.equalsIgnoreCase("splashscreen")) {
                appConfig.setSplashScreen(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("splashscreen_res")) {
                appConfig.setmSplashScreenRes(sb.toString().trim());
            } else if (localName.equalsIgnoreCase("dateformat")) {
                int dateFormat = (int) (Float.parseFloat(sb.toString().trim()));
                appConfig.setDateFormat(dateFormat);
            } else if (localName.equalsIgnoreCase("showSidebar")) {
                int showSidebar = (int) (Float.parseFloat(sb.toString().trim()));
                if (showSidebar == 1)
                    appConfig.setShowSidebar(true);
                else
                    appConfig.setShowSidebar(false);
            } else if (localName.equalsIgnoreCase("sidebarItem")) {
                appConfig.setShowSidebar(true);
                appConfig.addSidebarItem(sidebarItem);
                sidebarItem = null;
            } else if (localName.equalsIgnoreCase("showmenu")) {
                int showMenu = (int) (Float.parseFloat(sb.toString().trim()));
                appConfig.setShowMenu(showMenu);
            } else if (localName.equalsIgnoreCase("image")) {
                appConfig.addImage(image);
                appConfig.addControl(image);
                image = null;
            } else if (localName.equalsIgnoreCase("x")) {
                if (image != null) {
                    try{
                        image.setLeft((int) (Float.parseFloat(sb.toString().trim())));
                    }catch(NumberFormatException nFEx){
                    }
                } else if (label != null) {
                    try{
                        label.setLeft((int) (Float.parseFloat(sb.toString().trim())));
                    }catch(NumberFormatException nFEx){
                    }
                } else if (button != null) {
                    try{
                        button.setLeft((int) (Float.parseFloat(sb.toString().trim())));
                    }catch(NumberFormatException nFEx){
                    }
                }
            } else if (localName.equalsIgnoreCase("y")) {
                if (image != null) {
                    try{
                        image.setTop((int) (Float.parseFloat(sb.toString().trim())));
                    }catch(NumberFormatException nFEx){
                    }
                } else if (label != null) {
                    try{
                        label.setTop((int) (Float.parseFloat(sb.toString().trim())));
                    }catch(NumberFormatException nFEx){
                    }
                } else if (button != null) {
                    try{
                        button.setTop((int) (Float.parseFloat(sb.toString().trim())));
                    }catch(NumberFormatException nFEx){
                    }
                }
            } else if (localName.equalsIgnoreCase("width")) {
                if (image != null) {
                    image.setWidth((int) (Float.parseFloat(sb.toString().trim())));
                } else if (label != null) {
                    label.setWidth((int) (Float.parseFloat(sb.toString().trim())));
                } else if (button != null) {
                    button.setWidth((int) (Float.parseFloat(sb.toString().trim())));
                }
            } else if (localName.equalsIgnoreCase("height")) {
                if (image != null) {
                    image.setHeight((int) (Float.parseFloat(sb.toString().trim())));
                } else if (label != null) {
                    label.setHeight((int) (Float.parseFloat(sb.toString().trim())));
                } else if (button != null) {
                    button.setHeight((int) (Float.parseFloat(sb.toString().trim())));
                }
            } else if (localName.equalsIgnoreCase("url")) {
                if (image != null) {
                    image.setSourceUrl(sb.toString().trim());
                } else if (inAdv) {
                    advData.setAdvType("url");
                    advData.setAdvContent(sb.toString().trim());
                } else if (widget != null) {
                    widget.setPluginUrl(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("imagedata")) {
                if (image != null) {
                    image.setmImageData(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("pushns")) {
                inPushNs = false;
            } else if (localName.equalsIgnoreCase("android_account")) {
                if (inPushNs) {
                    appConfig.setPushNotificationAccount(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("sender_id")) {
                if (inPushNs) {
                    appConfig.setPushNotificationAccount(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("gps_object")) {
                appConfig.addGPSNotification(gpsItem);
                gpsItem = null;
            } else if (localName.equalsIgnoreCase("latitude")) {
                if (gpsItem != null) {
                    gpsItem.setLatitude(
                            new Double(sb.toString().trim()).doubleValue());
                }
            } else if (localName.equalsIgnoreCase("longitude")) {
                if (gpsItem != null) {
                    gpsItem.setLongitude(
                            new Double(sb.toString().trim()).doubleValue());
                }
            } else if (localName.equalsIgnoreCase("title")) {
                if (gpsItem != null) {
                    gpsItem.setTitle(Html.fromHtml(sb.toString().trim()).toString());
                } else if (widget != null) {
                    widget.setTitle(Html.fromHtml(sb.toString().trim()).toString());
                } else if (label != null) {
                    label.setTitle(Html.fromHtml(sb.toString().trim()).toString());
                }
            } else if (localName.equalsIgnoreCase("description")) {
                if (gpsItem != null) {
                    gpsItem.setDescription(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("radius")) {
                if (gpsItem != null) {
                    gpsItem.setRadius(new Integer(sb.toString().trim()).intValue());
                }
            } else if (localName.equalsIgnoreCase("show")) {
                if (gpsItem != null) {
                    if (sb.toString().trim().equalsIgnoreCase("plural")) {
                        gpsItem.setCountOfView(GPSItem.Show.PLURAL);
                    }
                }
            } else if (localName.equalsIgnoreCase("adv")) {
                inAdv = false;
            } else if (localName.equalsIgnoreCase("type")) {
                if (inAdv) {
                    advData.setAdvType(sb.toString().trim());
                } else if (widget != null) {
                    widget.setmPluginType(sb.toString().trim());
                } else if (inPlugin) {
                    type = sb.toString().trim();
                }
            } else if (localName.equalsIgnoreCase("url_on_click")) {
                if (inAdv) {
                    advData.setAdvRedirect(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("html_on_click")) {
                if (inAdv) {
                    advData.setAdvRedirect(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("html")) {
                if (inAdv) {
                    advData.setAdvType("html");
                    advData.setAdvContent(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("uid")) {
                if (inAdv) {
                    advData.setAdvContent(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("apId")) {
                if (inAdv) {
                    advData.setAdvApId(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("adSpaceId")) {
                if (inAdv) {
                    advData.setAdvAdSpaceId(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("publisherId")) {
                if (inAdv) {
                    advData.setAdvPublisherId(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("label")) {
                if (button != null) {
                    button.setTitle(Html.fromHtml(sb.toString().trim()).toString());
                } else if (tab != null) {
                    tab.setLabel(sb.toString().trim());
                } else if(sidebarItem != null) {
                    sidebarItem.setLabel(sb.toString().trim());
                } else {
                    appConfig.addLabel(label);
                    appConfig.addControl(label);
                    label = null;
                }
            } else if (localName.equalsIgnoreCase("size")) {
                if (label != null) {
                    label.setFontSize((int) (Float.parseFloat(sb.toString().trim())));
                } else if (button != null) {
                    button.setFontSize((int) (Float.parseFloat(sb.toString().trim())));
                }
            } else if (localName.equalsIgnoreCase("color")) {
                if (label != null) {
                    label.setColor(sb.toString().trim());
                } else if (button != null) {
                    button.setColor(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("style")) {
                if (label != null) {
                    label.setStyle(sb.toString().trim());
                } else if (button != null) {
                    button.setStyle(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("button")) {
                appConfig.addButton(button);
                appConfig.addControl(button);
                button = null;
            } else if (localName.equalsIgnoreCase("icon")) {
                if (button != null) {
                    button.setImageSourceUrl(sb.toString().trim());
                } else if (tab != null) {
                    tab.setIconUrl(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("icondata")) {
                if (button != null) {
                    button.setmImageData(sb.toString().trim());
                } else if (tab != null) {
                    tab.setmIconData(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("icon_res"))
            {
                if (button != null) {
                    button.setmImageData_res(sb.toString().trim());
                } else if (tab != null) {
                    tab.setmIconData_res(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("image_res"))
            {
                if ( image != null ) {
                    image.setmImageData_res(sb.toString().trim());
                }
            }
            else if (localName.equalsIgnoreCase("align")) {
                if (button != null) {
                    button.setAlign(sb.toString().trim());
                }
                if (label != null) {
                    label.setAlign(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("valign")){
                if (button != null) {
                    button.setVAlign(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("paddingX")) {
                if(button != null){
                    try{
                        button.setPaddingX(Integer.parseInt(sb.toString().trim()));
                    }catch(Exception ex){
                    }
                }
            } else if (localName.equalsIgnoreCase("paddingY")) {
                if(button != null){
                    try{
                        button.setPaddingY(Integer.parseInt(sb.toString().trim()));
                    }catch(Exception ex){
                    }
                }
            } else if (localName.equalsIgnoreCase("func")) {
                if (button != null) {
                    button.setOrder((int) (Float.parseFloat(sb.toString().trim())));
                } else if (tab != null) {
                    tab.setOrder((int) (Float.parseFloat(sb.toString().trim())));
                } else if (sidebarItem != null) {
                    sidebarItem.setOrder(Integer.valueOf(sb.toString().trim()));
                } else if (inPlugin) {
                    func = sb.toString().trim();
                }
            } else if (localName.equalsIgnoreCase("tabitem")) {
                appConfig.addTab(tab);
                tab = null;
            } else if (localName.equalsIgnoreCase("plugin")) {
                if (plugins.containsKey(func)) {
                    plugins.get(func).add(type);
                } else {
                    plugins.put(func, new ArrayList<String>());
                    plugins.get(func).add(type);
                }
                inPlugin = false;
            } else if (localName.equalsIgnoreCase("widget")) {
                appConfig.addWidget(widget);
                widget = null;
            } else if (localName.equalsIgnoreCase("order")) {
                if (widget != null) {
                    widget.setOrder((int) (Float.parseFloat(sb.toString().trim())));
                }
            } else if (localName.equalsIgnoreCase("name")) {
                if (widget != null) {
                    widget.setPluginName(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("package")) {
                if (widget != null) {
                    widget.setPluginPackage(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("hash")) {
                if (widget != null) {
                    widget.setPluginHash(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("background")) {
                if (widget != null) {
                    widget.setBackground(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("textcolor")) {
                if (widget != null) {
                    widget.setTextColor(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("data")) {
                if (widget != null) {
                    widget.setPluginXmlData(sb.toString().trim());
//                    String md5Data = Utils.md5(widget.getPluginXmlData());
//                    widget.setUpdated(ConfigDBHelper.hasWidgetChanged(ctx, widget.getWidgetId(), md5Data));
                }
            } else if (localName.equalsIgnoreCase("subtitle")) {
                if (widget != null) {
                    widget.setSubtitle(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("favicon")) {
                if (widget != null) {
                    widget.setFaviconURL(sb.toString().trim());
                }
            } else if (localName.equalsIgnoreCase("sidebar")) {
                if (widget != null) {
                    if (sb.toString().trim().compareTo("1") == 0)
                        widget.setAddToSidebar(true);
                    else
                        widget.setAddToSidebar(false);
                }
            } else if (localName.equalsIgnoreCase("navBar")) {
                if (designer != null) {
                    appConfig.setNavBarDesign(designer);
                    designer = null;
                }
            } else if (localName.equalsIgnoreCase("tabBar")) {
                if (designer != null) {
                    appConfig.setTabBarDesign(designer);
                    designer = null;
                }
            } else if (localName.equalsIgnoreCase("bottomBar")) {
                if (designer != null) {
                    appConfig.setBottomBarDesign(designer);
                    designer = null;
                }
            } else if (localName.equalsIgnoreCase("item")) {
                itemPosition = null;
            } else
//            if (localName.equalsIgnoreCase("logo")) {
//                appConfig.getLoginScreen().setLogo(sb.toString().trim());
//            } else if (localName.equalsIgnoreCase("useFacebook")) {
//                appConfig.getLoginScreen().setUseFacebook(sb.toString().trim().equalsIgnoreCase("true"));
//            } else if (localName.equalsIgnoreCase("useTwitter")) {
//                appConfig.getLoginScreen().setUseTwitter(sb.toString().trim().equalsIgnoreCase("true"));
//            } else if (localName.equalsIgnoreCase("useEmail")) {
//                appConfig.getLoginScreen().setUseEmail(sb.toString().trim().equalsIgnoreCase("true"));
//            } else if (localName.equalsIgnoreCase("allowSignUp")) {
//                appConfig.getLoginScreen().setAllowSignup(sb.toString().trim().equalsIgnoreCase("true"));
//            } else if (localName.equalsIgnoreCase("signupEndpoint")) {
//                appConfig.getLoginScreen().setSignupEndpoint(sb.toString().trim());
//            } else if (localName.equalsIgnoreCase("loginEndpoint")) {
//                appConfig.getLoginScreen().setLoginEndpoint(sb.toString().trim());
//            } else if (localName.equalsIgnoreCase("recoveryPasswordEndpoint")) {
//                appConfig.getLoginScreen().setRecoveryPasswordEndpoint(sb.toString().trim());
//            } else if (localName.equalsIgnoreCase("appId")) {
//                appConfig.getLoginScreen().setAppId(sb.toString().trim());
//            } else
                if (localName.equalsIgnoreCase("widget_id")) {
                    if(widget != null) {
                        try {
                            widget.setWidgetId(Integer.parseInt(sb.toString().trim()));
                        } catch (Exception ex) {
                            Log.e("", "");
                        }
                    }
                } else if (localName.equalsIgnoreCase("update_content_push_enabled")) {
                    try {
                        appConfig.setUpdateContentPushEnabled(sb.toString().trim().equalsIgnoreCase("1"));
                    } catch (Exception ex) {
                        Log.e("", "");
                    }
                } else if(localName.equalsIgnoreCase("endpoint")) {
                    if(loginForm != null) {
                        loginForm.setEndpoint(sb.toString().trim());
                    }
                } else if(localName.equalsIgnoreCase("color1")) {
                    if(loginForm != null) {
                        try {
                            loginForm.addSchemeColor("color1", Color.parseColor(sb.toString().trim()));
                        } catch (Exception ex) {
                            Log.e("", "");
                        }
                    }
                } else if(localName.equalsIgnoreCase("color2")) {
                    if(loginForm != null) {
                        try {
                            loginForm.addSchemeColor("color2", Color.parseColor(sb.toString().trim()));
                        } catch (Exception ex) {
                            Log.e("", "");
                        }
                    }
                } else if(localName.equalsIgnoreCase("color3")) {
                    if(loginForm != null) {
                        try {
                            loginForm.addSchemeColor("color3", Color.parseColor(sb.toString().trim()));
                        } catch (Exception ex) {
                            Log.e("", "");
                        }
                    }
                } else if(localName.equalsIgnoreCase("color4")) {
                    if(loginForm != null) {
                        try {
                            loginForm.addSchemeColor("color4", Color.parseColor(sb.toString().trim()));
                        } catch (Exception ex) {
                            Log.e("", "");
                        }
                    }
                } else if(localName.equalsIgnoreCase("color5")) {
                    if(loginForm != null) {
                        try {
                            loginForm.addSchemeColor("color5", Color.parseColor(sb.toString().trim()));
                        } catch (Exception ex) {
                            Log.e("", "");
                        }
                    }
                } else if(localName.equalsIgnoreCase("mainScreen")) {
                    if(loginForm != null) {
                        loginForm.setMainScreen(sb.toString().trim().equalsIgnoreCase("1"));
                    }
                } else if(localName.equalsIgnoreCase("widget_order")) {
                    if(loginForm != null && !loginForm.isMainScreen()) {
                        loginForm.addWidget(Integer.parseInt(sb.toString().trim()));
                    }
                } else if(localName.equalsIgnoreCase("loginScreen")) {
                    if(loginForm != null) {
                        appConfig.setLoginForm(loginForm);
                        loginForm = null;
                    }
                }

            sb.setLength(0);
            System.gc();
        }

    }
}
