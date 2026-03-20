package commons;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

/**
 * The entity class for the Note
 */
@Entity
public class Note extends NoteIdentification {

    @Column(columnDefinition = "TEXT")
    public String content;

    // Constructors
    public Note(String title, Collection collection, String content)  {
        super(title, collection);
        this.content = content;
    }

    public Note() {}

    // Override equals, hashCode, and toString
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}