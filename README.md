# Allrightsy
A utility for collecting third-party software license information

## Building
Build the uberjar:

    mvn package -Puberjar

## Running

      allrightsy.sh <packs> <outfile.json>

#### Program Arguments:    
 * `packs` : a comma-separated list of inputs to allrightsy. these inputs are supported:
   * /path/to/pom.xml       -- a maven pom.xml file
   * /path/to/node_modules  -- an npm node_modules directory
   * /path/to/licenses.json -- a verbatim set of license JSON entries to include
    
* `outfile.json` : the output file to write. it will be an array of JSON objects, 
                 each representing a piece of third-party software and its associated license.
    
