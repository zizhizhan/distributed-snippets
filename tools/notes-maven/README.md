

#### Get Repository

```bash
export MVN_REPO=`mvn help:evaluate -Dexpression=settings.localRepository | grep -v '[INFO]' | tail -n 1`
```