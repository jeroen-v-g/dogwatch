# DogWatch: Simple face recognition

Docker image with UI for face recognition: 
* Publish port 8080 and run with privileged option, connect via a browser. 
* Default username and password within the app is: admin:admin 
* Upload one or more faces to search for. 
* Connect to windows/smb/cifs shares and specify the search directories. 
* Search for the uploaded faces in the selected shares and directories. 

It is possible to mount a host share directly via docker to /tmp/dogwatch/mount/{sharename}