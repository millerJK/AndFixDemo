package com.example.javris.andfix.entity;

public class VersionEntity {

    public String app_url;
    public String patch_url;
    public String version_code;
    public String version_patch;
    public String remark;
    public String version_type;

    public VersionEntity() {
    }

    public VersionEntity(String app_url, String patch_url, String version_code
            , String version_patch, String remark, String version_type) {

        this.app_url = app_url;
        this.patch_url = patch_url;
        this.version_code = version_code;
        this.version_patch = version_patch;
        this.remark = remark;
        this.version_type = version_type;
    }

    @Override
    public String toString() {
        return "VersionEntity{" +
                "app_url='" + app_url + '\'' +
                ", patch_url='" + patch_url + '\'' +
                ", version_code='" + version_code + '\'' +
                ", version_patch='" + version_patch + '\'' +
                ", remark='" + remark + '\'' +
                ", version_type='" + version_type + '\'' +
                '}';
    }
}
