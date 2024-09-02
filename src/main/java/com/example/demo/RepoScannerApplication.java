package com.example.demo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RepoScannerApplication implements CommandLineRunner{
	public static final String APIBASE = "https://api.github.com/users/";
	
	public static void main(String[] args) {
		SpringApplication.run(RepoScannerApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		System.out.println("========== Welcome to GitHub Scanner ==========");
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Which user's repositories do you want to scan?");
		System.out.print("User name: ");
		GitUser gitUser = new GitUser(scanner.nextLine());
		scanner.close();
		
		HttpClient client = HttpClient.newBuilder().build();
		
		HttpRequest reposRequest = HttpRequest.newBuilder()
				.uri(new URI(APIBASE + gitUser.userName + "/repos"))
				.header("Accept", "application/json")
				.GET()
				.build();
		
		HttpResponse<String> response = client.send(reposRequest, BodyHandlers.ofString());
		if(response.body().charAt(0) == '[') {
			System.out.println("Object is and JSONArray");
			JSONArray repos = new JSONArray(response.body());
			System.out.println("User "+ gitUser.userName + " has " + repos.length() + " repositories.\n");
			for(int x = 0, idx = 0; x < repos.length(); x++) {
				
				if (repos.getJSONObject(x).get("fork").toString() == "false") {
					gitUser.repositories.add(repos.getJSONObject(x).get("name").toString());
									
					HttpRequest newRequest = HttpRequest.newBuilder()
							.uri(new URI("https://api.github.com/repos/" + gitUser.userName + "/" + gitUser.repositories.get(idx) + "/branches"))
							.header("Accept", "application/json")
							.GET()
							.build();
					HttpResponse<String> newResponse = client.send(newRequest, BodyHandlers.ofString());
					
					JSONArray branches = new JSONArray(newResponse.body());
					System.out.println("Repository: " + gitUser.repositories.get(idx) + " has " + branches.length() + " branch(es):");
					
					for(int y = 0; y < branches.length(); y++) {
						String branchName = branches.getJSONObject(y).get("name").toString();					
						String commitSha = branches.getJSONObject(y).getJSONObject("commit").get("sha").toString();
						
						
						System.out.println("    branch: " + branchName);
						System.out.println("    SHA: " + commitSha + "\n");
					}
					idx++;
				}
				else {
					System.out.println("Repository: " + repos.getJSONObject(x).get("name") + " is a FORK.");
				}
			}
		}
		else {
			JSONObject userResponse = new JSONObject(response.body());
			System.out.println(userResponse.get("status"));
			System.out.println(userResponse.get("message"));
		}		
	}
}
