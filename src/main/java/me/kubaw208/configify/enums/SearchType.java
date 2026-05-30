package me.kubaw208.configify.enums;

/**
 * Specifies the method for searching for configuration files in the directory structure.
 */
public enum SearchType {

    /**
     * Search directly in the specified folder.
     */
    FILES,

    /**
     * Search in the subfolders of the specified folder.
     */
    RECURSIVE

}