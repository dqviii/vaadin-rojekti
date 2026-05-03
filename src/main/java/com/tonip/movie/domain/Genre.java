package com.tonip.movie.domain;

import com.tonip.base.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.hibernate.envers.Audited;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Audited
@Table(name = "genre")
public class Genre extends AuditableEntity {

    public static final int GENRE_NAME_MAX_LENGTH = 60;
    public static final int DESCRIPTION_MAX_LENGTH = 5000;
    public static final int ICON_CODE_MAX_LENGTH = 40;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id")
    private Long id;

    @NotBlank
    @Size(max = GENRE_NAME_MAX_LENGTH)
    @Column(name = "genre_name", nullable = false, unique = true, length = GENRE_NAME_MAX_LENGTH)
    private String genreName = "";

    @NotBlank
    @Size(max = DESCRIPTION_MAX_LENGTH)
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description = "";

    @NotBlank
    @Size(max = ICON_CODE_MAX_LENGTH)
    @Column(name = "icon_code", nullable = false, length = ICON_CODE_MAX_LENGTH)
    private String iconCode = "";

    @NotNull
    @Column(name = "mainstream", nullable = false)
    private Boolean mainstream;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", nullable = false, length = 20)
    private TargetAudience targetAudience;

    public Genre() {
    }

    public Genre(String genreName, String description, String iconCode,
                 Boolean mainstream, TargetAudience targetAudience) {
        this.genreName = genreName;
        this.description = description;
        this.iconCode = iconCode;
        this.mainstream = mainstream;
        this.targetAudience = targetAudience;
    }

    public Long getId() {
        return id;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconCode() {
        return iconCode;
    }

    public void setIconCode(String iconCode) {
        this.iconCode = iconCode;
    }

    public Boolean getMainstream() {
        return mainstream;
    }

    public void setMainstream(Boolean mainstream) {
        this.mainstream = mainstream;
    }

    public TargetAudience getTargetAudience() {
        return targetAudience;
    }

    public void setTargetAudience(TargetAudience targetAudience) {
        this.targetAudience = targetAudience;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Genre other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
