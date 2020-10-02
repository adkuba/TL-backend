# Table of Contents
* [What I've learned](#what-Ive-learned)
* [Localhost](#localhost)
* [Wiki](#wiki)
  * [SSL](#ssl)
* [Docker](#docker)

## What I've learned
* How to create backend application in **Spring Boot, Java**
* Working with **MongoDB** and **Docker**
* Deploying to **Google App Engine** on custom domain with **SSL** or to Digital Ocean **virtual machine** with SSL<br>

To learn more visit [frontend repository](https://github.com/adkuba/TL-frontend). Backend is avaiable on <code>https://www.api.tline.site</code>

![meta](https://storage.googleapis.com/tline-files/meta.png)

## Localhost
On default project uses Atlas Mongodb, you can change configuration to run all in localhost. If you use localhost remember to run mongodb <code>sudo systemctl start mongod</code> Also remember to remove Google Cloud App Engine configuration, as project on default deploys to this platform. Backend can also run on typical virtual machine with SSL certificate, which you need to put in <code>resources/api-certificate.p12</code>

## Wiki
Read [wiki](https://github.com/adkuba/TL-backend/wiki) to learn more about backend functionality.

### SSL
Important! Check types of SSL certificates when buying! Some certificates are only for one exact domain for example <code>www.tline.site</code> and it will not work with subdomains. For main domain and all subdomains buy wildcart certificate! <br>
Generating .p12 from SSL certificate <code>openssl pkcs12 -export -out certificate.p12 -inkey private.key -in certificate.crt -certfile CACert.crt</code>

## Docker
To run on virtual machine I'm using Docker. Steps in main directory:
- build image: <code>docker build -t tl-backend .</code>
- export to file: <code>docker save -o ./tl-backend.tar tl-backend</code>
- transfer exported .tar to your virtual machine example <code>scp tl-backend.tar root@164.90.194.108:~/tl-backend.tar</code>
- load tar <code>docker load -i tl-backend.tar</code>
- run in background exposing ports <code>docker run -d --rm -p 80:8081 -p 443:8081 tl-backend</code>
- check if is running <code>docker ps -a</code>
- stop <code>docker stop \<name\></code>
