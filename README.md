# kneedle4j

[![CI](https://github.com/shravanasati/kneedle4j/actions/workflows/ci.yml/badge.svg)](https://github.com/shravanasati/kneedle4j/actions/workflows/ci.yml)
[![Code Quality](https://github.com/shravanasati/kneedle4j/actions/workflows/quality.yml/badge.svg)](https://github.com/shravanasati/kneedle4j/actions/workflows/quality.yml)
[![Java CI](https://img.shields.io/badge/Java-8%2B-brightgreen)](https://openjdk.java.net/)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-blue)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.txt)

A robust Java implementation of the **Kneedle algorithm** for automatic detection of knee/elbow points in curves. This library is based on the research paper ["Finding a 'Kneedle' in a Haystack: Detecting Knee Points in System Behavior"](https://raghavan.usc.edu/papers/kneedle-simplex11.pdf) by Ville Satopää et al.

## 📦 Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.github.shravanasati</groupId>
    <artifactId>kneedle4j</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.github.shravanasati:kneedle4j:1.0-SNAPSHOT'
```

## 🚀 Quick Start

### Basic Usage

```java
import com.github.shravanasati.kneedle4j.*;

// Your data points
double[] x = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
double[] y = {1, 2, 3, 5, 8, 13, 18, 20, 21, 21.5};

// Create KneeLocator with default parameters
KneeLocator kl = new KneeLocator(x, y);

// Get the knee point
Double kneeX = kl.getKnee();
Double kneeY = kl.getKneeY();

System.out.println("Knee found at: (" + kneeX + ", " + kneeY + ")");
```

### Advanced Usage

```java
// Customize the knee detection
KneeLocator kl = new KneeLocator(
    x, y,                                    // Data points
    1.0,                                     // Sensitivity (S parameter)
    Enums.CURVE_TYPE.CONCAVE,               // Curve type
    Enums.DIRECTION.INCREASING,             // Direction
    Enums.INTERPOLATION_METHOD.POLYNOMIAL,  // Interpolation method
    false,                                  // Online mode
    7                                       // Polynomial degree
);

// Get all detected knees
Set<Double> allKnees = kl.getAllKnees();
List<Double> allKneeYs = kl.getAllKneesY();
```

### Automatic Shape Detection

The library can automatically detect the curve type and direction:

```java
import com.github.shravanasati.kneedle4j.ShapeDetector;

// Detect curve characteristics
ShapeDetector.ShapeResult shape = ShapeDetector.findShape(x, y);
Enums.DIRECTION direction = shape.getDirection();
Enums.CURVE_TYPE curveType = shape.getCurveType();

// Use detected parameters
KneeLocator kl = new KneeLocator(x, y, curveType, direction);
```

## 🧪 Testing

Run the comprehensive test suite:

```bash
./mvnw test
```
## 📄 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

## 🔗 References

- **Original Paper**: [Finding a 'Kneedle' in a Haystack](https://raghavan.usc.edu/papers/kneedle-simplex11.pdf) by Ville Satopää et al.
- **Python Implementation**: [kneed library](https://github.com/arvkevi/kneed/) by Kevin Arvai
- **Apache Commons Math**: [Mathematical and statistical components](https://commons.apache.org/proper/commons-math/)
