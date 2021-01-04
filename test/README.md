
cd /usr/local/Cursos/WebFlux/lab04/test

docker-compose build test

docker-compose up --abort-on-container-exit --exit-code-from test && docker-compose down


