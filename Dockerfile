# This is a Dockerfile for the dogwatch image.

FROM ubuntu:latest

RUN apt-get -y update

# Added python-3 setuptools cifs-utils smbclient
RUN DEBIAN_FRONTEND="noninteractive" \
    apt-get install -y --fix-missing \
    build-essential \
    cmake \
    cifs-utils \
    gfortran \
    git \
    jetty9 \
    wget \
    curl \
    graphicsmagick \
    libgraphicsmagick1-dev \
    libatlas-base-dev \
    # libavcodec-dev \
    # libavformat-dev \
    # libgtk2.0-dev \
    libjpeg-dev \
    liblapack-dev \
    libswscale-dev \
    maven \
    pkg-config \
    python3-dev \
    python3-numpy \
    python3-pip \
    python3-setuptools \
    smbclient \
    software-properties-common \
    && apt-get clean && rm -rf /tmp/* /var/tmp/*
RUN cd ~ && \
    mkdir -p dlib && \
    git clone -b 'v19.9' --single-branch https://github.com/davisking/dlib.git dlib/ && \
    cd  dlib/ && \
    python3 setup.py install --yes USE_AVX_INSTRUCTIONS && \
    cd ~ && \
    rm -rf ~/dlib

RUN cd ~ && \
    mkdir face_recognition && \
    git clone https://github.com/ageitgey/face_recognition.git

RUN cd ~ && \
    mkdir dogwatch && \
    git clone https://github.com/jeroen-v-g/dogwatch.git

RUN cp ~/dogwatch/src/py/face_recognition/face_recognition_cli.py ~/face_recognition/face_recognition/face_recognition_cli.py
    
RUN cd ~/face_recognition && \
    pip3 install -r requirements.txt && \
    python3 setup.py install

RUN cd ~/dogwatch && \
    mvn package && \
    cp ~/dogwatch/target/DogWatch.war /usr/share/jetty9/webapps/ROOT.war && \
    mvn clean && \ 
    rm -rf ~/.m2

RUN mkdir /usr/share/java/etc && \
    cp /usr/share/jetty9/etc/webdefault.xml /usr/share/java/etc/webdefault.xml

ENTRYPOINT cd /usr/share/jetty9 && \
    java -jar start.jar
