package com.hongking.hktpms.Utils;

import java.util.ArrayList;

public class RspAppVersionFir {
//	
//	version	String	版本
//	changelog	String	更新日志
//	versionShort	String	版本编号(兼容旧版字段)
//	build	String	编译号
//	installUrl	String	安装地址（兼容旧版字段）
//	install_url	String	安装地址(新增字段)
//	update_url	String	更新地址(新增字段)
//	binary	Object	更新文件的对象，仅有大小字段fsize
	private String name;
	private String version;
	private String changelog;
	private String updated_at;
	private String versionShort;
	private String build;
	private String installUrl;
	private String install_url;
	private String direct_install_url;
	private String update_url;
	private Binary binary;
	
	
	

	public String getName() {
		return name;
	}




	public void setName(String name) {
		this.name = name;
	}




	public String getVersion() {
		return version;
	}




	public void setVersion(String version) {
		this.version = version;
	}




	public String getChangelog() {
		return changelog;
	}




	public void setChangelog(String changelog) {
		this.changelog = changelog;
	}




	public String getUpdated_at() {
		return updated_at;
	}




	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}




	public String getVersionShort() {
		return versionShort;
	}




	public void setVersionShort(String versionShort) {
		this.versionShort = versionShort;
	}




	public String getBuild() {
		return build;
	}




	public void setBuild(String build) {
		this.build = build;
	}




	public String getInstallUrl() {
		return installUrl;
	}




	public void setInstallUrl(String installUrl) {
		this.installUrl = installUrl;
	}




	public String getInstall_url() {
		return install_url;
	}




	public void setInstall_url(String install_url) {
		this.install_url = install_url;
	}




	public String getDirect_install_url() {
		return direct_install_url;
	}




	public void setDirect_install_url(String direct_install_url) {
		this.direct_install_url = direct_install_url;
	}




	public String getUpdate_url() {
		return update_url;
	}




	public void setUpdate_url(String update_url) {
		this.update_url = update_url;
	}




	public Binary getBinary() {
		return binary;
	}




	public void setBinary(Binary binary) {
		this.binary = binary;
	}




	public class Binary{
		private String fsize;
	}
}
