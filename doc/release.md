# How to release and publish all artifacts of the credible-doc projects

## Prepare a new release

1. Commit all changes
    
2. Perform the release
    
    ```bash
    mvn clean --batch-mode release:prepare release:perform
    ```

## Publish artifacts to Maven Central Repository and push tags

1. Sign and deploy artifacts
    
    ```bash
    mvn clean deploy -P gpg
    ```
        
2. Push changes into the remote repository
    
    ```bash
    git push
    git push --tags
    ```   

## Prepare next version

1. Prepare the next SNAPSHOT version
    
    ```bash
    mvn --batch-mode release:update-versions -DdevelopmentVersion=1.0.xx-SNAPSHOT
    ```
2. Commit and push the new SNAPSHOT version by IDE or by GIT command line

3. Install jar files with the new SNAPSHOT version to the local .m2 repository

    ```bash
    mvn clean install -DskipTests=true -fae
    ```

4. Released artifacts will be located at `${basedir}/target/checkout/*/target`

## Rollback the release

1. Clean release plugin files
    
    ```bash
    mvn release:clean
    ```
   
2. Reset local repository commits
    
    ```bash
    git reset --hard origin/master
    ```
   
3. Remove local tag of unreleased version
    
    ```bash
    git tag --delete v1.0.xx
    ```
