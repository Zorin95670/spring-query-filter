#!/usr/bin/env python3
"""
Automatically aligns:
  - the project's <version>
  - the <java.version> property
with the spring-boot-dependencies version declared in pom.xml.

Usage:
    python align_spring_version.py pom.xml .github/spring-java-compat.yml

Output (stdout): a text summary + GitHub Actions output variables
(GITHUB_OUTPUT) to drive the rest of the workflow:
    new_version=4.1.0
    new_java=21
    java_bumped=true|false
    compat_unknown=true|false
"""

import sys
import os
import re
import xml.etree.ElementTree as ET
import yaml

NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def fail(msg: str) -> None:
    print(f"::error::{msg}")
    sys.exit(1)


def get_spring_boot_version(root: ET.Element) -> str:
    """Reads the spring-boot-dependencies version from dependencyManagement."""
    deps = root.findall(
        ".//m:dependencyManagement/m:dependencies/m:dependency", NS
    )
    for dep in deps:
        group_id = dep.find("m:groupId", NS)
        artifact_id = dep.find("m:artifactId", NS)
        version = dep.find("m:version", NS)
        if (
            group_id is not None
            and artifact_id is not None
            and version is not None
            and group_id.text == "org.springframework.boot"
            and artifact_id.text == "spring-boot-dependencies"
        ):
            return version.text
    fail("Could not find org.springframework.boot:spring-boot-dependencies in dependencyManagement")


def ensure_clean_release_version(version: str) -> None:
    """
    Ensures the Spring Boot version is a clean release version (e.g. 4.0.5),
    rejecting milestones, release candidates, or snapshots
    (e.g. 4.1.0-RC1, 4.1.0-M1, 4.1.0-SNAPSHOT).
    """
    if not re.match(r"^[0-9]+\.[0-9]+\.[0-9]+$", version):
        fail(
            f"Spring Boot version '{version}' is not a clean release version "
            f"(expected format X.Y.Z). Refusing to align on a milestone, "
            f"release candidate, or snapshot version."
        )


def major_minor(version: str) -> str:
    """4.0.5 -> '4.0'."""
    match = re.match(r"^(\d+)\.(\d+)", version)
    if not match:
        fail(f"Unexpected Spring Boot version: {version}")
    return f"{match.group(1)}.{match.group(2)}"


def load_compat_map(path: str) -> dict:
    if not os.path.exists(path):
        fail(f"Compatibility mapping file not found: {path}")
    with open(path, "r", encoding="utf-8") as f:
        data = yaml.safe_load(f) or {}
    # YAML keys like "3.4" may be parsed as float; normalize to str
    return {str(k): int(v) for k, v in data.items()}


def update_pom(pom_path: str, raw: str, new_version: str, new_java: str | None) -> str:
    """
    Updates pom.xml as raw text (no full ElementTree rewrite, to avoid
    breaking existing formatting/comments).
    """
    # 1. Project <version>...</version> — only the first occurrence,
    #    right after the project's own <artifactId> (no <parent> in this case).
    pattern_version = re.compile(
        r"(<artifactId>spring-query-filter</artifactId>\s*<version>)[^<]+(</version>)"
    )
    new_raw, count = pattern_version.subn(rf"\g<1>{new_version}\g<2>", raw, count=1)
    if count != 1:
        fail("Could not locate the project's <version> tag to update")
    raw = new_raw

    # 2. <java.version>...</java.version>
    if new_java is not None:
        pattern_java = re.compile(r"(<java\.version>)[^<]+(</java\.version>)")
        new_raw, count = pattern_java.subn(rf"\g<1>{new_java}\g<2>", raw, count=1)
        if count != 1:
            fail("Could not locate the <java.version> tag to update")
        raw = new_raw

    return raw


def main() -> None:
    if len(sys.argv) != 3:
        fail("Usage: align_spring_version.py <pom.xml> <compat_map.yml>")

    pom_path, compat_path = sys.argv[1], sys.argv[2]

    with open(pom_path, "r", encoding="utf-8") as f:
        raw_pom = f.read()

    root = ET.fromstring(raw_pom)
    spring_version = get_spring_boot_version(root)
    ensure_clean_release_version(spring_version)
    mm = major_minor(spring_version)

    compat_map = load_compat_map(compat_path)
    required_java = compat_map.get(mm)

    current_java_match = re.search(r"<java\.version>(\d+)</java\.version>", raw_pom)
    if not current_java_match:
        fail("Could not read <java.version> in pom.xml")
    current_java = int(current_java_match.group(1))

    compat_unknown = required_java is None
    java_bumped = False
    new_java_value = None

    if compat_unknown:
        print(
            f"::warning::Spring Boot version {spring_version} ({mm}) is missing from "
            f"the Java compatibility mapping. Manual review required."
        )
    else:
        if current_java < required_java:
            java_bumped = True
            new_java_value = str(required_java)
            print(
                f"Java {current_java} -> {required_java} "
                f"(required by Spring Boot {mm})"
            )
        else:
            print(
                f"Java {current_java} is already compatible with Spring Boot {mm} "
                f"(minimum required: {required_java})"
            )

    updated_pom = update_pom(pom_path, raw_pom, spring_version, new_java_value)
    with open(pom_path, "w", encoding="utf-8") as f:
        f.write(updated_pom)

    print(f"Project version aligned with Spring Boot: {spring_version}")

    github_output = os.environ.get("GITHUB_OUTPUT")
    if github_output:
        with open(github_output, "a", encoding="utf-8") as f:
            f.write(f"new_version={spring_version}\n")
            f.write(f"new_java={new_java_value or current_java}\n")
            f.write(f"java_bumped={'true' if java_bumped else 'false'}\n")
            f.write(f"compat_unknown={'true' if compat_unknown else 'false'}\n")


if __name__ == "__main__":
    main()
