FROM gradle:8.14-jdk21 AS build

WORKDIR /app

COPY --chown=gradle:gradle gradle gradle/
COPY --chown=gradle:gradle gradlew build.gradle.kts ./
COPY --chown=gradle:gradle src/ src/

RUN gradle build -x test --no-daemon

FROM gradle:8.14-jdk21

WORKDIR /app

COPY --from=build /app /app

CMD gradle ${TASK} -D"base.url=${BASE_URL}" --no-daemon; \
    mkdir -p /app/test-reports && \
    cp -r build/reports/* /app/test-reports/ 2>/dev/null || true
