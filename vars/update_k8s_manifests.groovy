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

            # IMPORTANT: Always sync with remote first
            git pull --rebase origin main

            #  Update Backend Image (safer regex)
            sed -i "s|image:.*krishibandhu-backend:.*|image: adarsh5559/krishibandhu-backend:${imageTag}|g" ${manifestsPath}/02-backend-deployment.yaml

            #  Update Frontend Image (safer regex)
            sed -i "s|image:.*krishibandhu-frontend:.*|image: adarsh5559/krishibandhu-frontend:${imageTag}|g" ${manifestsPath}/03-frontend-deployment.yaml

            #  Optional: update ingress
            if [ -f "${manifestsPath}/08-ingress.yaml" ]; then
                sed -i "s|host: .*|host: krishibandhu.adtechs.xyz|g" ${manifestsPath}/08-ingress.yaml
            fi

            # Check for changes
            if git diff --quiet; then
                echo "No changes to commit"
            else
                git add ${manifestsPath}/*.yaml
                git commit -m "Update frontend & backend image to ${imageTag} [ci skip]"

                #  Use credentials safely
                git remote set-url origin https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/Adarsh097/KrishiBandhu-Deployment.git

                #  Push
                git push origin HEAD:main
            fi
        """
    }
}
