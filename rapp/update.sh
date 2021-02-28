#!/bin/bash

# Update particle-trieur from the github releases page and the nightly artifact

cd /opt/particle-trieur

# Get latest main release
/usr/bin/curl -s https://api.github.com/repos/microfossil/particle-trieur/releases \
| grep "browser_download_url.*\ParticleTrieur.jar" \
| head -1 \
| cut -d ":" -f 2,3 \
| tr -d \" \
| wget -N -i -

# Get latest dev release
/usr/bin/curl -s https://api.github.com/repos/microfossil/particle-trieur/releases \
| grep "browser_download_url.*\ParticleTrieur-dev.jar" \
| head -1 \
| cut -d ":" -f 2,3 \
| tr -d \" \
| wget -N -i -

# Get nightly
wget -N -O nightly.zip https://nightly.link/microfossil/particle-trieur/workflows/nightly-artifact/dev/nightly.zip
unzip -o nightly.zip
