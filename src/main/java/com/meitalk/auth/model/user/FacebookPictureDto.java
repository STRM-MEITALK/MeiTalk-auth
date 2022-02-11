package com.meitalk.auth.model.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class FacebookPictureDto {
    private String height;
    private String is_silhouette;
    private String url;
    private String width;
}
