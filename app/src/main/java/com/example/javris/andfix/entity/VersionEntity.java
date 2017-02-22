package com.example.javris.andfix.entity;

public class VersionEntity {

    public String app_url; //app 下载地址
    public String patch_url; // 补丁包下载地址
    public String version_code; //开发最新app版本号
    public String version_patch; //最新补丁包版本号
    public String remark;  //更新提示内容
    public String version_type; //1.强制更新 2.及时更新 3.不进行更新

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
