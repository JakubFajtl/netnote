package commons;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

@MappedSuperclass
public class NoteIdentification
        implements Comparable<NoteIdentification> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    @Column(columnDefinition = "TEXT")
    public String title;

    @ManyToOne
    @JoinColumn(nullable = false)
    public Collection collection;

    public NoteIdentification(String title, Collection collection) {
        this.title = title;
        this.collection = collection;
    }

    public NoteIdentification(Note note) {
        this.id = note.id;
        this.title = note.title;
        this.collection = note.collection;
    }

    public NoteIdentification() {
    }

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

    //for testing without server
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int compareTo(NoteIdentification other) {
        return title.toLowerCase().compareTo(other.title.toLowerCase());
    }
}
