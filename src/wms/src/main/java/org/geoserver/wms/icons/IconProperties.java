package org.geoserver.wms.icons;

import java.util.Map;

import org.geoserver.ows.URLMangler.URLType;
import org.geoserver.ows.util.ResponseUtils;
import org.geotools.styling.Style;

public abstract class IconProperties {
    private IconProperties() {
    }
    
    public abstract Double getOpacity();
    public abstract Double getScale();
    public abstract Double getHeading();
    
    public abstract String href(String baseURL, String styleName);
    public abstract Style inject(Style base);
    public abstract Map<String, String> getProperties();

    
    public static IconProperties generator(final Double opacity, final Double scale, final Double heading, final Map<String, String> styleProperties) {
        return new IconProperties() {
            @Override
            public Double getOpacity() {
                return opacity;
            }

            @Override
            public Double getScale() {
                return scale;
            }

            @Override
            public Double getHeading() {
                return heading;
            }

            @Override
            public String href(String baseURL, String styleName) {
                return ResponseUtils.buildURL(baseURL, "rest/render/kml/icon/" + styleName, styleProperties, URLType.RESOURCE);
            }
            
            @Override
            public Style inject(Style base) {
                return IconPropertyInjector.injectProperties(base, styleProperties);
            }
            
            @Override
            public Map<String, String> getProperties() {
                return styleProperties;
            }
        };
    }
}
