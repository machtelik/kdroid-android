package org.kde.kdroid;

interface KDroidServiceApi {

	void setPort(int port);
	
	void returnLatestMessage(String address);

}