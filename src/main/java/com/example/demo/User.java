package com.example.demo;

import java.util.ArrayList;

class GitUser {
	String userName;
	ArrayList<String> repositories;
	
	GitUser(String selectedUserName) {
		userName = selectedUserName;
		repositories = new ArrayList<>();
	}
	
}