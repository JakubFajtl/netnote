package commons;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

public class CollectionConfig extends Collection
        implements Comparable<CollectionConfig>{

    //client side collection title
    public String title;
    public String serverURL;

    @JsonCreator
    public CollectionConfig(
            @JsonProperty("collectionKey") String collectionKey,
            @JsonProperty("title") String title,
            @JsonProperty("serverURL") String serverURL){
        super(collectionKey);
        this.title = title;
        this.serverURL = serverURL;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CollectionConfig that = (CollectionConfig) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(this.serverURL, that.serverURL);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }

    @Override
    public int compareTo(CollectionConfig other) {
        return title.toLowerCase().compareTo(other.title.toLowerCase());
    }
}
