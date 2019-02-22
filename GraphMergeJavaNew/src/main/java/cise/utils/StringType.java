package cise.utils;

import java.io.Serializable;

/**
 * 表示边或节点类型 实际为封装后的String
 */
public final class StringType implements Serializable{

    public final String value;

    public StringType(String s) {
        if (s == null) {
            this.value = "";
        } else {
            this.value = s;
        }
    }

    /**
     * 默认类型，字符串为空
     *
     * @return 一个默认类型
     */
    public static StringType defult() {
        return new StringType("");
    }

    public static StringType entryType = new StringType("ENTRY");

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StringType that = (StringType) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
