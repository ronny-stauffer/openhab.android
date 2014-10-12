package org.openhab.habdroid.model.topview;

import android.content.res.AssetManager;
import android.util.Xml;

import org.openhab.habdroid.model.topview.common.StreamUtil;
import org.openhab.habdroid.model.topview.common.StringUtil;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by staufferr on 10.10.2014.
 */
public class TopViewSVGToButtonParser {
    // We don't use namespaces so far
    private static final String namespace = null; //XmlPullParser.NO_NAMESPACE;

    public Map<String, TopViewButtonDescriptor> parseAsset(AssetManager assetManager, String fileName) {
        if (assetManager == null) {
            throw new NullPointerException("assetManager must not be undefined!");
        }
        if (StringUtil.isStringUndefinedOrEmpty(fileName)) {
            throw new IllegalArgumentException("fileName must not be undefined or empty!");
        }

        Map<String, TopViewButtonDescriptor> buttonDescriptors;

        InputStream stream = null;
        try {
            stream = assetManager.open(fileName);
        } catch (IOException e) {
            throw new TopViewParsingException("I/O error while accessing top view definition asset!");
        }
        try {
            buttonDescriptors = parse(stream);
        } finally {
            StreamUtil.closeStream(stream);
        }

        return buttonDescriptors;
     }

    public Map<String, TopViewButtonDescriptor> parse(InputStream stream) throws TopViewParsingException {
        if (stream == null) {
            throw new NullPointerException("stream must not be undefined!");
        }

        try {
            XmlPullParser parser = Xml.newPullParser();
            try {
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

                parser.setInput(stream, /* Encoding: */ null);

                parser.nextTag();

                return readSVG(parser);
            } catch (XmlPullParserException e) {
                throw new TopViewParsingException("Cannot parse top view definition!", e);
            } catch (IOException e) {
                throw new TopViewParsingException("I/O error while parsing top view definition!", e);
            }
        } finally {
//            StreamUtil.closeStream(stream);
        }
    }

    private Map<String, TopViewButtonDescriptor> readSVG(XmlPullParser parser) throws XmlPullParserException, IOException {
        final Map<String, TopViewButtonDescriptor> emptyMap = new HashMap<String, TopViewButtonDescriptor>();

        parser.require(XmlPullParser.START_TAG, namespace, "svg");
        while (!(parser.next() == XmlPullParser.END_TAG && "svg".equals(parser.getName()))) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            // Starts by looking for the "Buttons" layer (group)
            if (name.equals("g")) {
                String groupType = parser.getAttributeValue(namespace, "inkscape:groupmode");
                String groupName = parser.getAttributeValue(namespace, "inkscape:label");
                if ("layer".equals(groupType) && "Items".equals(groupName)) {
                    String groupTransform = parser.getAttributeValue(namespace, "transform");
                    if (groupTransform != null) {
                        throw new TopViewParsingException("Transformations aren't supported yet!");
                    }

                    return readButtonLayer(parser);
                }
            } else {
                //skip(parser);
            }
        }

        return emptyMap;
    }

    private Map<String, TopViewButtonDescriptor> readButtonLayer(XmlPullParser parser) throws XmlPullParserException, IOException {
        Map<String, TopViewButtonDescriptor> buttonDescriptors = new HashMap<String, TopViewButtonDescriptor>();

        parser.require(XmlPullParser.START_TAG, namespace, "g");
        while (!(parser.next() == XmlPullParser.END_TAG && "g".equals(parser.getName()))) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            // Starts by looking for a rectangular representing a button
            if (name.equals("rect")) {
                TopViewButtonDescriptor buttonDescriptor = readButtonRect(parser);
                if (buttonDescriptor != null) {
                    if (!buttonDescriptors.containsKey(buttonDescriptor.getItem())) {
                        buttonDescriptors.put(buttonDescriptor.getItem(), buttonDescriptor);
                    } else {
                        throw new TopViewParsingException("Multiple buttons per item aren't supported yet!");
                    }
                }
            }
        }

        return buttonDescriptors;
    }

    private TopViewButtonDescriptor readButtonRect(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, namespace, "rect");

        String rectXPosition = parser.getAttributeValue(namespace, "x");
        String rectYPosition = parser.getAttributeValue(namespace, "y");
        String rectWidth = parser.getAttributeValue(namespace, "width");
        String rectHeight = parser.getAttributeValue(namespace, "height");
        String rectDescription = null;

        while (!(parser.next() == XmlPullParser.END_TAG && "rect".equals(parser.getName()))) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();
            if (name.equals("desc")) {
                rectDescription = readDescription(parser);
            }
        }

        if (rectDescription != null) {
            int rectXPositionDecimalPointIndex = rectXPosition.indexOf('.');
            String rectXPositionInteger = rectXPositionDecimalPointIndex != -1 ? rectXPosition.substring(0, rectXPositionDecimalPointIndex) : rectXPosition;
            int buttonXPosition = Integer.valueOf(rectXPositionInteger);

            int rectYPositionDecimalPointIndex = rectYPosition.indexOf('.');
            String rectYPositionInteger = rectYPositionDecimalPointIndex != -1 ? rectYPosition.substring(0, rectYPositionDecimalPointIndex) : rectYPosition;
            int buttonYPosition = Integer.valueOf(rectYPositionInteger);

            int rectWidthDecimalPointIndex = rectWidth.indexOf('.');
            String rectWidthInteger = rectWidthDecimalPointIndex != -1 ? rectWidth.substring(0, rectWidthDecimalPointIndex) : rectWidth;
            int buttonWidth = Integer.valueOf(rectWidthInteger);

            int rectHeightDecimalPointIndex = rectHeight.indexOf('.');
            String rectHeightInteger = rectHeightDecimalPointIndex != -1 ? rectHeight.substring(0, rectHeightDecimalPointIndex) : rectHeight;
            int buttonHeight = Integer.valueOf(rectHeightInteger);

            return TopViewButtonDescriptor.create(buttonXPosition, buttonYPosition, buttonWidth, buttonHeight, /* item: */ rectDescription);
        }

        return null;
    }

    private String readDescription(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, namespace, "desc");

        while (!(parser.next() == XmlPullParser.END_TAG && "desc".equals(parser.getName()))) {
            if (parser.getEventType() != XmlPullParser.TEXT) {
                continue;
            }

            String descriptionText = parser.getText();

            if (!StringUtil.isStringUndefinedOrEmpty(descriptionText)) {
                return descriptionText;
            }
        }

        return null;
    }
}