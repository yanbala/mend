Hi, 
The project is written and build with java correto 11 <br/>
In order to build it:
1. make sure coretto 11 is installed
2. trigger shadowJar gradle task

<b>Executing</b></br>
1. build the jar with gradle as explained above (there is a compiled jar in the artifacts directory which you can use instead of building )
2. copy the jar into c:\development directory
3. copy artifacts/githubCLI.bat to c:\development directory
4. add c:\development directory to the PATH environment variable

<b>What is not implemented</b><br/>
Due to some time constraints I didn't get the chance to enrich
the --help message. Currently when using the --help flag there 
is a default help message.<br/>
Assuming this involves some configuration and more reading about
Picolci, I put the emphasis on coding the logic.

Sample output files are added to the artifacts' directory.


Cheers,
Yaniv.



