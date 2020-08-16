# Module plantuml-core

This module contains services for generating [plantUml](http://plantuml.com) diagrams
from plantUml notations.

For launching the generator, the Graphviz tool should be installed,
see the [http://plantuml.com/graphviz-dot](http://plantuml.com/graphviz-dot) documentation.
Graphviz is optional if you only need sequence diagrams and activity (beta) diagrams.

## Downloading
This module can be downloaded from the
[Maven Central Repository plantuml-core](https://mvnrepository.com/artifact/com.credibledoc/plantuml-core)

Maven dependency:

    <dependency>
        <groupId>com.credibledoc</groupId>
        <artifactId>plantuml-core</artifactId>
        <version>1.0.20</version>
    </dependency>

## Example of usage
    String svg = SvgGeneratorService.getInstance().generateSvgFromPlantUml(plantUml);

Where the _**plantUml**_ parameter can be, for example,

    Bob -> Alice : hello
    Alice -> Bob : hi

Then the returned _**String svg**_ in this case will be

    <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" contentScriptType="application/ecmascript" contentStyleType="text/css" height="159px" preserveAspectRatio="none" style="width:114px;height:159px;" version="1.1" viewBox="0 0 114 159" width="114px" zoomAndPan="magnify">
      <defs>
        <filter height="300%" id="f0yx89m" width="300%" x="-1" y="-1">
          <feGaussianBlur result="blurOut" stdDeviation="2.0"/>
          <feColorMatrix in="blurOut" result="blurOut2" type="matrix" values="0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 .4 0"/>
          <feOffset dx="4.0" dy="4.0" in="blurOut2" result="blurOut3"/>
          <feBlend in="SourceGraphic" in2="blurOut3" mode="normal"/>
        </filter>
      </defs>
      <g>
        <line style="stroke: #A80036; stroke-width: 1.0; stroke-dasharray: 5.0,5.0;" x1="29" x2="29" y1="39.6094" y2="120.3125"/>
        <line style="stroke: #A80036; stroke-width: 1.0; stroke-dasharray: 5.0,5.0;" x1="85" x2="85" y1="39.6094" y2="120.3125"/>
        <rect fill="#FEFECE" filter="url(#f0yx89m)" height="31.6094" style="stroke: #A80036; stroke-width: 1.5;" width="39" x="8" y="3"/>
        <text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacingAndGlyphs" textLength="25" x="15" y="24.5332">Bob</text>
        <rect fill="#FEFECE" filter="url(#f0yx89m)" height="31.6094" style="stroke: #A80036; stroke-width: 1.5;" width="39" x="8" y="119.3125"/>
        <text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacingAndGlyphs" textLength="25" x="15" y="140.8457">Bob</text>
        <rect fill="#FEFECE" filter="url(#f0yx89m)" height="31.6094" style="stroke: #A80036; stroke-width: 1.5;" width="44" x="61" y="3"/>
        <text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacingAndGlyphs" textLength="30" x="68" y="24.5332">Alice</text>
        <rect fill="#FEFECE" filter="url(#f0yx89m)" height="31.6094" style="stroke: #A80036; stroke-width: 1.5;" width="44" x="61" y="119.3125"/>
        <text fill="#000000" font-family="sans-serif" font-size="14" lengthAdjust="spacingAndGlyphs" textLength="30" x="68" y="140.8457">Alice</text>
        <polygon fill="#A80036" points="73,67.6094,83,71.6094,73,75.6094,77,71.6094" style="stroke: #A80036; stroke-width: 1.0;"/>
        <line style="stroke: #A80036; stroke-width: 1.0;" x1="29.5" x2="79" y1="71.6094" y2="71.6094"/>
        <text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacingAndGlyphs" textLength="27" x="36.5" y="67.1045">hello</text>
        <polygon fill="#A80036" points="40.5,97.9609,30.5,101.9609,40.5,105.9609,36.5,101.9609" style="stroke: #A80036; stroke-width: 1.0;"/>
        <line style="stroke: #A80036; stroke-width: 1.0;" x1="34.5" x2="84" y1="101.9609" y2="101.9609"/>
        <text fill="#000000" font-family="sans-serif" font-size="13" lengthAdjust="spacingAndGlyphs" textLength="10" x="46.5" y="97.4561">hi</text>
    <!--
    !WARNING! Original strings (double dash) has been replaced by '- -' (dash+space+dash) in this comment, because the string (double dash) is not permitted within comments. And link parameters, for example ?search=... have also been REMOVED from the comment, because they are not readable to humans.
    <img uml="
    @startuml
    Bob -> Alice : hello
    Alice -> Bob : hi
    @enduml
    "/>
    --></g>
    </svg>

And generated svg file then looks like the next example

![An UML diagram that describes, how the generated content looks like](doc/img/example.svg?sanitize=true)

## Parent module
See the [plantuml-parent README.md](../README.md) file.