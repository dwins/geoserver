package org.geoserver.wms.icons;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public final class IconRenderer {
    private final static ReferencedEnvelope sampleArea = new ReferencedEnvelope(-1, 1, -1, 1, null);
    private final static SimpleFeatureCollection sampleData;
    
    static {
        SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
        typeBuilder.setName("example");
        typeBuilder.setNamespaceURI("http://example.com/");
        typeBuilder.setSRS("EPSG:4326");
        typeBuilder.add("the_geom", Point.class);
        GeometryFactory geomFactory = new GeometryFactory();
        SimpleFeatureType featureType = typeBuilder.buildFeatureType();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        featureBuilder.set("the_geom", geomFactory.createPoint(new Coordinate(0, 0)));
        MemoryFeatureCollection temp = new MemoryFeatureCollection(featureType);
        temp.add(featureBuilder.buildFeature(null));
        sampleData = temp;
    }
    
    /**
     * Render a point icon for the given style. This operation will fail if any
     * style properties in the given style are dynamic. This method is intended
     * to work with styles that have been preprocessed by IconPropertyExtractor
     * and IconPropertyInjector.
     * 
     * @param style
     * @return
     */
    public static BufferedImage renderIcon(Style style) {
        int size = findIconSize(style) + 1; // size is an int because icons are always square
        MapContent mapContent = new MapContent();
        mapContent.addLayer(new FeatureLayer(sampleData, style));
        BufferedImage image = new BufferedImage(size * 4 + 1, size * 4 + 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.scale(4, 4);
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setMapContent(mapContent);
        try {
            try {
                renderer.paint(graphics, new Rectangle(size, size), sampleArea);
            } finally {
                graphics.dispose();
            }
        } finally {
            mapContent.dispose();
        }
        return image;
    }

    private static int findIconSize(Style style) {
        int size = 0;
        if (style.featureTypeStyles().size() == 0) throw new IllegalArgumentException("NO EMPTY STYLES");
        for (FeatureTypeStyle ftStyle : style.featureTypeStyles()) {
            if (ftStyle.rules().size() == 0) throw new IllegalArgumentException("NO EMPTY FTSTYLES");
            for (Rule rule : ftStyle.rules()) {
                if (rule.symbolizers().size() == 0) throw new IllegalArgumentException("NO EMPTY RULES");
                for (Symbolizer symbolizer : rule.symbolizers()) {
                    if (symbolizer instanceof PointSymbolizer) {
                        Graphic g = ((PointSymbolizer) symbolizer).getGraphic();
                        if (g != null) {
                            Double rotation = g.getRotation() != null ? g.getRotation().evaluate(null, Double.class) : null;
                            size = Math.max(size, getGraphicSize(g, rotation));
                            if (!g.graphicalSymbols().isEmpty()) {
                                if (g.graphicalSymbols().get(0) instanceof Mark) {
                                    Mark mark = (Mark) g.graphicalSymbols().get(0);
                                    if (mark.getStroke() != null) {
                                        
                                    }
                                }
                            }
                        }
                    } else {
                        throw new IllegalArgumentException("IconRenderer only supports PointSymbolizer");
                    }
                }
            }
        }
        return size;
    }
    
    private static int getGraphicSize(Graphic g, Double rotation) {
        final int baseSize;
        final int strokePadding;
        if (g.getSize() == null) {
            baseSize = 16; // TODO: Account for external graphics
        } else {
            Integer calculated = g.getSize().evaluate(null, Integer.class);
            if (calculated != null) {
                baseSize = calculated;
            } else {
                baseSize = 16;
            }
        }
        if (g.graphicalSymbols().isEmpty()) {
            strokePadding = 0;
        } else if (!(g.graphicalSymbols().get(0) instanceof Mark)) {
            strokePadding = 0;
        } else {
            Mark mark = (Mark) g.graphicalSymbols().get(0);
            if (mark.getStroke() != null && mark.getStroke().getWidth() != null) {
                strokePadding = mark.getStroke().getWidth().evaluate(null, Integer.class);
            } else {
                strokePadding = 0;
            }
        }
        int paddedSize = baseSize + Math.round(strokePadding / 2f);
        if (rotation != null) {
            double factor = Math.abs(Math.sin(Math.toRadians(rotation))) +
                            Math.abs(Math.cos(Math.toRadians(rotation)));
            paddedSize *= factor;
        }
        return paddedSize;
    }
}
