package org.geoserver.wms.icons;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
            
            @Override
            public String getIconName(Style style) {
                try {
                    final MessageDigest digest = MessageDigest.getInstance("MD5");
                    digest.update(style.getName().getBytes("UTF-8"));
                    for (Map.Entry<String, String> property : styleProperties.entrySet()) {
                        digest.update(property.getKey().getBytes("UTF-8"));
                        digest.update(property.getValue().getBytes("UTF-8"));
                    }
                    final byte[] hash = digest.digest();
                    final StringBuilder builder = new StringBuilder();
                    for (byte b : hash) {
                        builder.append(String.format("%02x", b));
                    }
                    return builder.toString();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public abstract String getIconName(Style style);
}
