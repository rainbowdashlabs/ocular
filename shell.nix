{ pkgs ? import <nixpkgs> {}, ... }:

let
jdk = pkgs.jdk17;
gradle= pkgs.gradle.override { java = jdk; };
in
pkgs.mkShell
{
  packages = with pkgs; [jdk gradle python311 mkdocs];
}

