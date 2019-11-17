FROM openjdk:11.0.3-jdk

ADD target/*jar /myapp/
ADD target/lib/* /myapp/lib/

RUN curl -ksL https://storage.googleapis.com/kubernetes-release/release/$(curl -ks https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl -o /root/kubectl

RUN chmod 755 /root/kubectl

