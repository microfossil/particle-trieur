# ParticleTrieur

ParticleTrieur is a cross-platform java program to help organise, label, process and classify images, particularly for particle samples such as microfossils.

It works with the MISO CNN training library (https://github.com/microfossil/particle-classification) to provide an easy interface to train image classification CNN models.

Please see the documentation and tutorials here:

https://particle-classification.readthedocs.io/en/latest/

# Compiling

## Building with IntelliJ

- Install Amazon Coretto 8
- Open this repository in IntelliJ IDEA
- Set compiler to Amazon Correto 8
- Build project
- Run project (if no configuration, add an 'Application' target with the main class set to particletrieur.App)

## Building single jar for deployment

- Open project in IntelliJ
- Press ctrl twice to open run command window
- Run `mvn clean package`
- Single jar will be created in target directory

# Licence

« The software « PARTICLE TRIEUR » has been developed at the laboratory
Centre Européen de recherche et d’Enseignement de Géosciences de
l’Environnement (CEREGE-UMR7330) by the following authors :
Mr Ross MARCHANT and Mr Thibault de GARIDEL THORON..

The software « PARTICLE TRIEUR » is covered by author rights which are
owned by the following public institutions :
CENTRE NATIONAL DE LA RECHERCHE SCIENTIFIQUE and AIX MARSEILLE UNIVERSITE,

The software « PARTICLE TRIEUR » 's source code has been filed on 03/07/2019
at the Agence de protection des programmes under the number :
IDDN.FR.001.280007.000.S.P.2019.000.31235 (Réf CNRS : DL12143-01).

The software « PARTICLE TRIEUR » is released under the
« GPL-2.0  licence » / « GPL-3.0  licence » (see LICENCE file)