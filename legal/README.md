Steps to update legal information

1. Update index.html with the new changes
2. Update privacy_policy_local_content string with new changes to reflect the content of index.html
   a. Best way is to create a variable and assign the html code as a string with triple quotes
   b. Use android studio build-in feature to extract string resource. This cleans the text from any
   invalid characters when creating the string resource.
3. Update PrivacyPolicyTermsOfServiceConfig effectiveDate value
4. Deploy index.html to hosting
   a. Open legal folder in terminal
   b. Use command `firebase init`
   c. Select hosting and continue the steps (use public folder, do not overrite existing index file)
   d. Use command `firebase deploy` to update website
