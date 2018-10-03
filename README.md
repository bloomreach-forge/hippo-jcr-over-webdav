[![Build Status](https://travis-ci.org/onehippo-forge/hippo-jcr-over-webdav.svg?branch=develop)](https://travis-ci.org/onehippo-forge/hippo-jcr-over-webdav)

# Hippo Repository JCR over WebDAV Support

This module supports JCR over WebDAV connections for Hippo Repository.
So, you can use JCR APIs against remote Hippo Repository via JCR over WebDAV connections by using this module.

# Documentation (Local)

The documentation can generated locally by this command:

```bash
$ mvn clean install
$ mvn clean site
```

The output is in the ```target/site/``` directory by default. You can open ```target/site/index.html``` in a browser.

# Documentation (GitHub Pages)

Documentation is available at [https://onehippo-forge.github.io/content-export-import/](https://onehippo-forge.github.io/content-export-import/).

You can generate the GitHub pages only from ```master``` branch by this command:

```bash
$ mvn clean install
$ find docs -name "*.html" -exec rm {} \;
$ mvn -Pgithub.pages clean site
```

The output is in the ```docs/``` directory by default. You can open ```docs/index.html``` in a browser.

You can push it and GitHub Pages will be served for the site automatically.
