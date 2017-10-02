
[![](https://travis-ci.org/nlpie/biomedicus-measures.svg?branch=master)](https://travis-ci.org/nlpie/biomedicus-measures)

# BioMedICUS Measures

A lightweight (small and dependency-free) Java 8 library for identifying and normalizing numbers and 
measurements in text. This was developed as a stand-alone component of 
[BioMedICUS](http://nlpie.github.io/biomedicus/), a biomedical and clinical NLP engine developed by 
the NLP-IE Group at the University of Minnesota Institute for Health Informatics.

This project makes use of the 2017 SPECIALIST Lexicon number files. For more information about the 
SPECIALIST Lexicon, see their 
[website](https://lsg3.nlm.nih.gov/LexSysGroup/Projects/lexicon/current/web/index.html) and their
[Terms & Conditions](https://lsg3.nlm.nih.gov/LexSysGroup/docs/TermsAndConditions.html).

## Using in your project

To use in a maven project, include the following in your pom: 

```xml
<dependencies>
  <dependency>
    <groupId>edu.umn.biomedicus</groupId>
    <artifactId>biomedicus-measures</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

Alternatively, download the .jar and include that in your libraries.

## Javadoc

You can find the api documentation for this project [here](https://nlpie.github.io/biomedicus-measures/site/apidocs/index.html)

## Detecting numbers in text

```java
Iterator<String> iterator = tokens.iterator();
String token = null;
while (true) {
  if (token == null) {
    if (!iterator.hasNext()) {
      break;
    }
    token = iterator.next();
  }

  int begin = tokenLabel.getBegin();
  int end = tokenLabel.getEnd();

  if (numberDetector.tryToken(text, begin, end)) {
    // do something with detected number
    if (!numberDetector.getConsumedLastToken()) {
      continue;
    }
  }

  token = null;
}

if (numberDetector.finish()) {
  // do something with detected number
}

```

## Detecting units of measurement in text
```java
for(String word : sentence) {
  Optional<Result> potentialResult = unitRecognizer.advance(word, index, index + word.length());
  if (potentialResult.isPresent()) {
    Result result = potentialResult.get();
    // do stuff with result.
  }
  index += word.length();
}
Optional<Result> potentialResult = unitRecognizer.finish();
if (potentialResult.isPresent()) {
  Result result = potentialResult.get();
  int begin = result.getBegin();
  int end = result.getEnd();
}

```


## Contact and Support
For issues or enhancement requests, feel free to submit to the Issues tab on GitHub.

BioMedICUS has a [gitter chat](https://gitter.im/biomedicus/biomedicus) and a 
[Google Group](https://groups.google.com/a/umn.edu/forum/#!forum/biomedicus) for contacting 
developers with questions, suggestions or feedback.

## About Us
BioMedICUS is developed by the
[University of Minnesota Institute for Health Informatics NLP/IE Group](http://www.bmhi.umn.edu/ihi/research/nlpie/)
with assistance from the
[Open Health Natural Language Processing \(OHNLP\) Consortium](http://ohnlp.org/index.php/Main_Page).

## Contributing

Anyone is welcome and encouraged to contribute. If you discover a bug, or think the project could 
use an enhancement, follow these steps: 

1. Create an issue and offer to code a solution. We can discuss the issue and decide whether any 
code would be a good addition to the project. 
2. Fork the project. [https://github.com/nlpie/biomedicus-measures/fork]
3. Create Feature branch (`git checkout -b feature-name`)
4. Code your solution. 
  - Follow the [Google style guide for Java](https://google.github.io/styleguide/javaguide.html). 
  There are IDE profiles available [here](https://github.com/google/styleguide).
  - Write unit tests for any non-trivial aspects of your code. If you are fixing a bug write a 
  regression test: one that confirms the behavior you fixed stays fixed.
5. Commit to branch. (`git commit -am 'Summary of changes'`)
6. Push to GitHub (`git push origin feature-name`)
7. Create a pull request on this repository from your forked project. We will review and discuss 
your code and merge it.
