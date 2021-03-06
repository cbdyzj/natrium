package nano.support;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Entity utils
 */
public abstract class EntityUtils {

    /**
     * @param sql SQL
     * @return slimmed SQL
     */
    public static @NotNull String slim(@NotNull String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * @param clazz entity class
     * @return column name list
     */
    public static @NotNull List<@NotNull String> entityColumnNames(@NotNull Class<?> clazz) {
        var mappedColumns = new ArrayList<String>();
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(clazz);
        for (var pd : pds) {
            // setter and getter exists
            if (pd.getWriteMethod() != null && pd.getReadMethod() != null) {
                String underscoredName = underscoreName(pd.getName());
                mappedColumns.add(underscoredName);
            }
        }
        return mappedColumns;
    }

    /**
     * Convert property name to underscore name
     *
     * @param name property name
     * @return underscore name
     */
    public static @NotNull String underscoreName(@Nullable String name) {
        if (!StringUtils.hasLength(name)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(lowerCaseName(name.substring(0, 1)));
        for (int i = 1; i < name.length(); i++) {
            String s = name.substring(i, i + 1);
            String slc = lowerCaseName(s);
            if (!s.equals(slc)) {
                result.append("_").append(slc);
            } else {
                result.append(s);
            }
        }
        return result.toString();
    }

    private static @NotNull String lowerCaseName(@NotNull String name) {
        return name.toLowerCase(Locale.US);
    }

    /**
     * Convert underscore name to property name
     *
     * @param name underscore name
     * @return property name
     */
    public static @NotNull String propertyName(@Nullable String name) {
        StringBuilder result = new StringBuilder();
        boolean nextIsUpper = false;
        if (name != null && name.length() > 0) {
            if (name.length() > 1 && name.charAt(1) == '_') {
                result.append(Character.toUpperCase(name.charAt(0)));
            } else {
                result.append(Character.toLowerCase(name.charAt(0)));
            }
            for (int i = 1; i < name.length(); i++) {
                char c = name.charAt(i);
                if (c == '_') {
                    nextIsUpper = true;
                } else {
                    if (nextIsUpper) {
                        result.append(Character.toUpperCase(c));
                        nextIsUpper = false;
                    } else {
                        result.append(Character.toLowerCase(c));
                    }
                }
            }
        }
        return result.toString();
    }

}
