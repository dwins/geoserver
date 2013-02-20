package org.geoserver.wms.icons;

import static org.junit.Assert.*;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory2;
import org.geotools.styling.Symbolizer;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import static org.geotools.filter.text.ecql.ECQL.toFilter;
import static org.geotools.filter.text.ecql.ECQL.toExpression;

public class IconPropertiesTest {
    private static final SimpleFeature fieldIs1;
    private static final SimpleFeature fieldIs2;
    private static final StyleFactory2 styleFactory = (StyleFactory2) CommonFactoryFinder.getStyleFactory();

    static {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("example");
        typeBuilder.setNamespaceURI("http://example.com/");
        typeBuilder.setSRS("EPSG:4326");
        typeBuilder.add("field", String.class);
        SimpleFeatureType featureType = typeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        featureBuilder.set("field", "1");
        fieldIs1 = featureBuilder.buildFeature(null);
        featureBuilder.set("field", "2");
        fieldIs2 = featureBuilder.buildFeature(null);
    }

    private String encode(Style style, SimpleFeature feature) {
        Map<String, String> iconProperties = IconPropertyExtractor.extractProperties(style, feature);
        return queryString(iconProperties);
    }

    private String queryString(Map<String, String> params) {
        try {
            StringBuilder buff = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    buff.append("&");
                }
                buff.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            return buff.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private final PointSymbolizer mark(String name, Color stroke, Color fill, float opacity, int size) {
        return SLD.pointSymbolizer(SLD.createPointStyle(name, stroke, fill, opacity, size));
    }

    private final PointSymbolizer externalGraphic(String url, String format) {
        ExternalGraphic exGraphic = styleFactory.createExternalGraphic(url, format);
        Graphic graphic = styleFactory.createGraphic(new ExternalGraphic[] { exGraphic }, null, null, null, null, null);
        return styleFactory.createPointSymbolizer(graphic, null);
    }

    private final PointSymbolizer grayCircle() {
        return mark("circle", Color.BLACK, Color.GRAY, 1f, 16);
    }

    private final Rule rule(Filter filter, Symbolizer... symbolizer) {
        Rule rule = styleFactory.createRule();
        rule.setFilter(filter);
        for (Symbolizer s : symbolizer) rule.symbolizers().add(s);
        return rule;
    }

    private final Rule catchAllRule(Symbolizer... symbolizer) {
        Rule rule = styleFactory.createRule();
        for (Symbolizer s : symbolizer) rule.symbolizers().add(s);
        return rule;
    }

    private final Rule elseRule(Symbolizer... symbolizer) {
        Rule rule = styleFactory.createRule();
        rule.setElseFilter(true);
        for (Symbolizer s : symbolizer) rule.symbolizers().add(s);
        return rule;
    }

    private final FeatureTypeStyle featureTypeStyle(Rule... rules) {
        FeatureTypeStyle ftStyle = styleFactory.createFeatureTypeStyle();
        for (Rule r : rules) ftStyle.rules().add(r);
        return ftStyle;
    }

    private final Style style(FeatureTypeStyle... ftStyles) {
        Style style = styleFactory.createStyle();
        for (FeatureTypeStyle f : ftStyles) style.featureTypeStyles().add(f);
        return style;
    }

    private final Style styleFromRules(Rule... rules) {
        return style(featureTypeStyle(rules));
    }

    @Test
    public void testSimpleStyleEncodesNoProperties() {
        final Style simple = styleFromRules(catchAllRule(grayCircle()));
        assertEquals("0.0.0=", encode(simple, fieldIs1));
    }

    @Test
    public void testFilters() throws CQLException {
        final PointSymbolizer symbolizer = grayCircle();
        final Rule a = rule(toFilter("field = 1"), symbolizer);
        final Rule b = rule(toFilter("field = 2"), symbolizer);
        Style style = styleFromRules(a, b);

        assertEquals("0.0.0=", encode(style, fieldIs1));
        assertEquals("0.1.0=", encode(style, fieldIs2));
    }

    @Test
    public void testMultipleSymbolizers() {
        final PointSymbolizer symbolizer = grayCircle();
        final Rule a = catchAllRule(symbolizer, symbolizer);
        final Style style = styleFromRules(a);

        assertEquals("0.0.0=&0.0.1=", encode(style, fieldIs1));
    }

    @Test
    public void testMultipleFeatureTypeStyle() {
        final PointSymbolizer symbolizer = grayCircle();
        final Style s = style(
                featureTypeStyle(catchAllRule(symbolizer)),
                featureTypeStyle(catchAllRule(symbolizer)));
        assertEquals("0.0.0=&1.0.0=", encode(s, fieldIs1));
    }

    @Test
    public void testElseFilter() throws CQLException {
        final PointSymbolizer symbolizer = grayCircle();
        final Style style = styleFromRules(rule(toFilter("field = 1"), symbolizer), elseRule(symbolizer));
        assertEquals("0.0.0=", encode(style, fieldIs1));
        assertEquals("0.1.0=", encode(style, fieldIs2));
    }

    @Test
    public void testDynamicMark() throws CQLException {
        final PointSymbolizer symbolizer = grayCircle();
        final Mark mark = (Mark) symbolizer.getGraphic().graphicalSymbols().get(0);
        mark.setWellKnownName(toExpression("if_then_else(equalTo(field, 1), 'circle', 'square')"));
        final Style s = styleFromRules(catchAllRule(symbolizer));
        assertEquals("0.0.0=&0.0.0.name=circle", encode(s, fieldIs1));
        assertEquals("0.0.0=&0.0.0.name=square", encode(s, fieldIs2));
    }

    @Test
    public void testDynamicOpacity() throws CQLException {
        final PointSymbolizer symbolizer = grayCircle();
        final Graphic graphic = symbolizer.getGraphic();
        graphic.setOpacity(toExpression("1 / field"));
        final Style s = styleFromRules(catchAllRule(symbolizer));
        assertEquals("0.0.0=&0.0.0.opacity=1.0", encode(s, fieldIs1));
        assertEquals("0.0.0=&0.0.0.opacity=0.5", encode(s, fieldIs2));
    }

    @Test
    public void testDynamicRotation() throws CQLException {
        final PointSymbolizer symbolizer = grayCircle();
        final Graphic graphic = symbolizer.getGraphic();
        graphic.setRotation(toExpression("45 * field"));
        final Style s = styleFromRules(catchAllRule(symbolizer));
        assertEquals("0.0.0=&0.0.0.rotation=45.0", encode(s, fieldIs1));
        assertEquals("0.0.0=&0.0.0.rotation=90.0", encode(s, fieldIs2));
    }

    @Test
    public void testDynamicSize() throws CQLException {
        final PointSymbolizer symbolizer = grayCircle();
        final Graphic graphic = symbolizer.getGraphic();
        graphic.setSize(toExpression("field * 16"));
        final Style s = styleFromRules(catchAllRule(symbolizer));
        assertEquals("0.0.0=&0.0.0.size=16.0", encode(s, fieldIs1));
        assertEquals("0.0.0=&0.0.0.size=32.0", encode(s, fieldIs2));
    }

    @Test
    public void testDynamicURL() throws CQLException, UnsupportedEncodingException {
        final PointSymbolizer symbolizer = externalGraphic("http://example.com/foo${field}.png", "image/png");
        final Style style = styleFromRules(catchAllRule(symbolizer));
        String url = URLEncoder.encode("http://example.com/", "UTF-8");
        assertEquals("0.0.0=&0.0.0.url=" + url + "foo1.png", encode(style, fieldIs1));
        assertEquals("0.0.0=&0.0.0.url=" + url + "foo2.png", encode(style, fieldIs2));
    }

    @Ignore
    public void testPublicURL() throws CQLException {
        // TODO: I need to write the API for getting an Icon URL instead of just the styling properties before this test can be implemented
        final PointSymbolizer symbolizer = externalGraphic("http://example.com/foo.png", "image/png");
        final Style style = styleFromRules(catchAllRule(symbolizer));
        assertEquals("http://example.com/foo.png", encode(style, fieldIs1));
    }
}
