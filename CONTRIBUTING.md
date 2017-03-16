Contributor Guidelines
===

Thank you for contributing to BetterChunkLoader! Please have a read through this document and make sure you understand it all
before contributing.

## Reporting an Issue, Requesting a Feature

When submitting an issue through please remember that we are volunteers, and as volunteers, we have
limited time to be able to help. We would love to spend as much time fixing your issues and implementing features
as possible. To that end, please keep the following in mind when reporting bugs and requesting features:

* Make the title of the report clear. "It's broken" does not help us much, "BetterChunkLoader reports an error when I try to use
/bcl info" is more helpful and lets us see what is wrong just by looking at the list.

* When reporting a bug:
    * Describe how you reproduce the bug step by step
    * Explain what you expect to happen
    * Explain what actually happens
    * Include screenshots or console output if it helps explain the issue
    (but do **not** take a screenshot of console output)

* When describing a feature:
    * Tell us what you'd like to see
    * Tell us why you want it, and what the use case is
    * Give us an idea on how it should work

* If we need more information about a bug or a feature, we'll ask for clarification! We want to get the system right.

For more information about writing a bug report in general, have a look at [this page](http://www.chiark.greenend.org.uk/~sgtatham/bugs.html),
particularly the summary at the bottom.

## Pull Requests

If you'd like to write code for us, great, welcome aboard! We have a few things that you should be aware of.

### Code Style

We tend to follow the [Google's Java Code Conventions](https://google.github.io/styleguide/javaguide.html) but some of
the more important things to note, including changes are:

* Line endings
    * Use Unix line endings when committing (\n).
    * Windows users of git can do `git config --global core.autocrlf true` to let git convert them automatically.

* Column width
    * 150 for Javadocs
    * 150 for code
    * Feel free to wrap when it will help with readability

* Indentation
    * Use 4 spaces for indentations, do not use tabs.

* File headers
    * File headers must contain the license headers for the project as defined in HEADER.txt.
    * You can use `gradle licenseFormat` to automatically to do this for you.

* Imports
    * Imports must be grouped in the following order:
        * normal imports
        * java imports
        * javax imports

* Javadocs
    * Do not use @author
    * Wrap additional paragraphs in `<p>` and `</p>`
    * Capitalize the first letter in the descriptions within each “at clause”,
    i.e. @param name Player to affect, no periods
    * Be descriptive when explaining the purpose of the class, interface,
    enum, method etc.

* Deprecation
    * Do not deprecate content unless you have a replacement, or if the provided feature is removed.
    * Be prepared to provide justification for the deprecation.
    * When deprecating, provide the month and year when it was deprecated.

Note that this style guide is _not_ a hard and fast requirement, but please keep your code style sane and similar
to ours.

### Code Conventions
* Return `java.util.Optional<>` if you are adding an API method that could return `null`.
* Only one declaration per line.
* All uppercase letters for a constant variable. Multiple words should be separated with an underscore - `_`.

### Submitting your Pull Requests
In your PRs, please make sure you fulfil the following:

* Provide a justification for the change - is it a new feature or a fix to a bug?
* Before sending a pull request ensure that your branch is up to date with the branch you are targeting.
* Do not squash commits unless directed to do so, but please _rebase_ your changes on top of master when you feel your changes are ready to be submitted - _do not merge_. We will squash the commits in a way we feel logical.