"# vimeotracking" 

Pre requisites:
1. Set up AWS access key and secret key in vimeo.properties under src/main/resources

The main method is called Worker.java under src/main/java/init

This project uses gradle for dependency management.

Known issues:
 Vimeo API rate limiting.  
 If a number of calls are made to the Vimeo API, it ends up throwing an error:
 {"developer_message":"Your application has made too many API requests. Please read our guidelines for more information: https://developer.vimeo.com/guidelines","link":null,"error_code":9003,"error":"Too many API requests. Wait an hour or so, then try again."}
 
 The guidelines suggests limiting the response from the API using "fields"  
 This has been done in Vimeo.searchVideos()  
 However, this might still result in an error if you run the application multiple times within a few minutes.  
 Ideally, this should run only once an hour, and this problem should not occur then.  
 AWS Lambda could be used to schedule it to run every hour.  
 
 
 
 
 