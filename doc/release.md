# How to release and publish all artifacts of the credible-doc projects

## Prepare a new release

1. Create a new version
    
    ```bash
    mvn versions:set -DnewVersion=1.0.xx
    ```
    
2. Clean backup files
    
    ```bash
    mvn versions:commit
    ```
    
3. Run tests and update `dependency-reduced-pom.xml`
    
    ```bash
    mvn clean install
    ```

## Publish artifacts to Maven Central Repository, commit and push a new tag

1. Sign and deploy artifacts
    
    ```bash
    mvn clean deploy -P gpg -DskipTests
    ```
        
2. Commit changes
    
    ```bash
    git commit -a -m "Version 1.0.xx released"
    ``` 

3. Create tag
    
    ```bash
    git tag -a v1.0.xx -m "Version 1.0.xx released"
    ``` 
        
4. Push changes into the remote repository
    
    ```bash
    git push
    git push origin v1.0.xx
    ```   

## Prepare next version

1. Prepare the next SNAPSHOT version (do not forget to increase version number)
    
    ```bash
    mvn versions:set -DnewVersion=1.0.xx-SNAPSHOT
    ```

2. Clean backup files

    ```bash
    mvn versions:commit
    ```

3. Install jar files with the new SNAPSHOT version to the local .m2 repository

    ```bash
    mvn clean install -DskipTests
    ```

4. Commit changes

    ```bash
    git commit -a -m "Version 1.0.xx-SNAPSHOT"
    ```

5. Push changes

    ```bash
    git push
    ```
## Generate documentation

## Rollback the tag in case of a mistake

1. Remove local tag of unreleased version
    
    ```bash
    git tag --delete v1.0.xx
    ```