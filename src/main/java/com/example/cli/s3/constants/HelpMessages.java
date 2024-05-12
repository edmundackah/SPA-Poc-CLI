package com.example.cli.s3.constants;

public interface HelpMessages {

    String ENV = "Target env (e.g. dev, staging, prod)";
    String BUILD_PATH = "Directory containing your static assets (e.g. dist)";
    String PREFIX = "The route to your SPA";
    String PAGE_SIZE = "Number of results per page (Defaults to one page)";
    String PAGE_NUMBER = "Page to navigate to (set to 0 to show all results without pagination)";
}
