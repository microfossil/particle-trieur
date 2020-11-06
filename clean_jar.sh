#!/bin/bash

zip -d out/artifacts/ParticleTrieur_jar/ParticleTrieur.jar 'META-INF/*.RSA'
#zip -dv out/artifacts/ParticleTrieur_jar/ParticleTrieur.jar 'META-INF/*.SF' 'META-INF/*.RSA'
zip -d target/ParticleTrieur-2.2.0.jar 'META-INF/*.SF' 'META-INF/*.RSA'