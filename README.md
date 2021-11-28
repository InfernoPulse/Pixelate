# Pixelate

This project is meant to allow you to pixelate an image using a quad tree. The program was written in Windows 10 using OpenJDK Runtime Environment Corretto-11.0.8.10.1 (build 11.0.8+10-LTS).

To run the program, first compile the java files using
```javac *.java```
Then pixelate an image as follows:
```java Pixelate <path of image> <pixelation depth>```
- the path of the image can be relative or absolute, but the slashes in the path must be forwards slashes and not backslashes
- the pixelation depth must be greater than or equal to 0 or lesser than the maximum pixelation depth
  - the maximum pixelation depth will be outputted if you input too high a depth