def call(Map config = [:]) {

    def imageTag = config.imageTag ?: error("Image tag is required")
    def manifestsPath = config.manifestsPath ?: 'kubernetes'
    def gitCredentials = config.gitCredentials ?: 'github-credentials'
    def gitUserName = config.gitUserName ?: 'Adarsh097'
    def gitUserEmail = config.gitUserEmail ?: 'adarshgupta0601@gmail.com'

    echo "Updating Kubernetes manifests with image tag: ${imageTag}"

    withCredentials([usernamePassword(
        credentialsId: gitCredentials,
        usernameVariable: 'GIT_USERNAME',
        passwordVariable: 'GIT_PASSWORD'
    )]) {

        sh """
            git config user.name "${gitUserName}"
            git config user.email "${gitUserEmail}"
        """

        sh """
            # Update Backend Image
            sed -i "s|image: adarsh5559/krishbandhu-backend:.*|image: adarsh5559/krishbandhu-backend:${imageTag}|g" ${manifestsPath}/02-backend-deployment.yaml

            # Update Frontend Image
            sed -i "s|image: adarsh5559/krishbandhu-frontend:.*|image: adarsh5559/krishbandhu-frontend:${imageTag}|g" ${manifestsPath}/03-frontend-deployment.yaml

            # Optional: update ingress
            if [ -f "${manifestsPath}/08-ingress.yaml" ]; then
                sed -i "s|host: .*|host: krishbandhu.adtechs.xyz|g" ${manifestsPath}/08-ingress.yaml
            fi

            # Check for changes
            if git diff --quiet; then
                echo "No changes to commit"
            else
                git add ${manifestsPath}/*.yaml
                git commit -m "Update frontend & backend image to ${imageTag} [ci skip]"

                git remote set-url origin https://Adarsh097:$GIT_PASSWORD@github.com/Adarsh097/KrishiBandhu-Deployment.git
                git push origin HEAD:main
            fi
        """
    }
}