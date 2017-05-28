package com.nezihyilmaz.coloredtabs;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class XmlParser {

    private XmlResourceParser parser;
    private Context context;

    XmlParser(Context ctx, int xmlResourceID){

        this.context=ctx;
        parser=ctx.getResources().getXml(xmlResourceID);

    }

    ArrayList<Tab> parseTabsXML(){

        ArrayList<Tab> tabs = new ArrayList<>();

        Resources resources=context.getResources();

        int eventType = -1;
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String locationValue = parser.getName();
                if (locationValue.equals("tab")) {

                    String title = parser.getAttributeValue(null, "title");
                    int icon = parser.getAttributeResourceValue(null,"icon",0);
                    int color = parser.getAttributeResourceValue(null,"color",0);
                    tabs.add(new Tab(null,resources.getDrawable(icon),resources.getColor(color),title));
                }
            }
            try {
                eventType = parser.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tabs;
    }

}
