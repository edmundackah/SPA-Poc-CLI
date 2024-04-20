# SPA Management and Deployment CLI PoC using Spring Shell

*Effortlessly manage and deploy your Single Page Applications (SPAs).*

> Note: This is a boilerplate project to show how one might go about setting up CLI in Spring Shell

## Key Features

* **Simplified SPA Deployments:** Streamline the deployment of your SPAs to S3 buckets with intuitive commands.
* **Multi-tenancy Support:** Seamlessly manage SPAs across multiple tenants, ensuring data isolation and customization capabilities.
* **Robust Audit Logs:** Maintain a detailed history of deployment actions and configuration changes for transparency and accountability.
* **Spring Shell Foundation:** Leverages the power and ease of Spring Shell for a user-friendly, interactive command-line experience.

### Initial Commands

* **Deployment**
    * `create-bucket <bucket-name>`: Creates a new S3 bucket.

* **Object Management**
  * `get-objects [environment]`: Retrieves a list of objects within an S3 bucket associated with a specified environment (defaults to "dev").

* **Miscellaneous**
    * `view-audit-log`: Shows the full audit log of CLI actions.

## Getting Started
### Prerequisites
    * Java JDK 17 or newer
    * Any S3 Compatible Object Storage Server