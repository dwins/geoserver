package org.geoserver.wms.icons;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.visitor.IsStaticExpressionVisitor;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.opengis.style.GraphicalSymbol;

final public class IconPropertyInjector {
    private final FilterFactory filterFactory;
    private final Map<String, String> properties;

    private IconPropertyInjector(Map<String, String> properties) {
        this.filterFactory = CommonFactoryFinder.getFilterFactory();
        this.properties = properties;
    }
    
    private List<List<MiniRule>> injectProperties(List<List<MiniRule>> ftStyles) {
        List<List<MiniRule>> result = new ArrayList<List<MiniRule>>();
        for (int i = 0; i <  ftStyles.size(); i++) {
            List<MiniRule> origRules = ftStyles.get(i);
            List<MiniRule> resultRules = new ArrayList<MiniRule>();
            for (int j = 0; j < origRules.size(); j++) {
                MiniRule origRule = origRules.get(j);
                List<PointSymbolizer> resultSymbolizers = new ArrayList<PointSymbolizer>();
                for (int k = 0; k < origRule.symbolizers.size(); k++) {
                    String key = i + "." + j + "." + k;
                    if (properties.containsKey(key)) {
                        PointSymbolizer ptSym = origRule.symbolizers.get(k);
                        injectPointSymbolizer(key, ptSym);
                        resultSymbolizers.add(ptSym);
                    }
                }
                resultRules.add(new MiniRule(null, false, resultSymbolizers));
            }
            result.add(resultRules);
        }
        return result;
    }
    
    private boolean isStatic(Expression ex) {
        return (Boolean) ex.accept(IsStaticExpressionVisitor.VISITOR, null);
    }

    private boolean shouldUpdate(String key, Expression exp) {
        return exp != null && properties.containsKey(key) && !isStatic(exp);
    }
    
    private Expression getLiteral(String key) {
        return filterFactory.literal(properties.get(key));
    }
    
    private void injectPointSymbolizer(String key, PointSymbolizer pointSymbolizer) {
        if (pointSymbolizer.getGraphic() != null) {
            injectGraphic(key, pointSymbolizer.getGraphic());
        }
    }
    
    private void injectGraphic(String key, Graphic graphic) {
        if (shouldUpdate(key + ".opacity", graphic.getOpacity())) {
            graphic.setOpacity(getLiteral(key + ".opacity"));
        }
        if (shouldUpdate(key + ".rotation", graphic.getRotation())) {
            graphic.setRotation(getLiteral(key + ".rotation"));
        }
        if (shouldUpdate(key + ".size", graphic.getSize())) {
            graphic.setSize(getLiteral(key + ".size"));
        }
        if (!graphic.graphicalSymbols().isEmpty()) {
            GraphicalSymbol symbol = graphic.graphicalSymbols().get(0);
            if (symbol instanceof Mark) {
                injectMark(key, (Mark) symbol);
            } else if (symbol instanceof ExternalGraphic) {
                injectExternalGraphic(key, (ExternalGraphic) symbol);
            }
        }
    }

    private void injectExternalGraphic(String key, ExternalGraphic symbol) {
        try {
            symbol.setLocation(new URL(properties.get(key + ".url")));
        } catch (MalformedURLException e) {
            // Just ignore the invalid URL
            // TODO: Log at finer or finest level?
        }
    }

    private void injectMark(String key, Mark mark) {
        if (shouldUpdate(key + ".name", mark.getWellKnownName())) {
            mark.setWellKnownName(getLiteral(key + ".name"));
        }
        if (mark.getFill() != null) {
            injectFill(key + ".fill", mark.getFill());
        }
        if (mark.getStroke() != null) {
            injectStroke(key + ".stroke", mark.getStroke());
        }
    }

    private void injectStroke(String key, Stroke stroke) {
        if (shouldUpdate(key + ".color", stroke.getColor())) {
            stroke.setColor(getLiteral(key + ".color"));
        }
        if (shouldUpdate(key + ".dashoffset", stroke.getDashOffset())) {
            stroke.setDashOffset(getLiteral(key + ".dashoffset"));
        }
        if (shouldUpdate(key + ".linecap", stroke.getLineCap())) {
            stroke.setLineCap(getLiteral(key + ".linecap"));
        }
        if (shouldUpdate(key + ".linejoin", stroke.getLineJoin())) {
            stroke.setLineJoin(getLiteral(key + ".linejoin"));
        }
        if (shouldUpdate(key + ".opacity", stroke.getOpacity())) {
            stroke.setOpacity(getLiteral(key + ".opacity"));
        }
        if (shouldUpdate(key + ".width", stroke.getWidth())) {
            stroke.setWidth(getLiteral(key + ".width"));
        }
        if (stroke.getGraphicStroke() != null) {
            injectGraphic(key + ".graphic", stroke.getGraphicStroke());
        }
        if (stroke.getGraphicFill() != null) {
            injectGraphic(key + ".graphic", stroke.getGraphicFill());
        }
    }

    private void injectFill(String key, Fill fill) {
        if (shouldUpdate(key + ".color", fill.getColor())) {
            fill.setColor(getLiteral(key + ".color"));
        }
        if (shouldUpdate(key + ".opacity", fill.getOpacity())) {
            fill.setOpacity(getLiteral(key + ".opacity"));
        }
        if (fill.getGraphicFill() != null) {
            injectGraphic(key + ".graphic", fill.getGraphicFill());
        }
    }

    public static Style injectProperties(Style style, Map<String, String> properties) {
        List<List<MiniRule>> ftStyles = MiniRule.minify(style);
        StyleFactory factory = CommonFactoryFinder.getStyleFactory();
        return MiniRule.makeStyle(factory, new IconPropertyInjector(properties).injectProperties(ftStyles));
    }
}
