import g4f
g4f.debug.logging = True # enable logging
g4f.check_version = False # Disable automatic version checking
print(g4f.version) # check version
print(g4f.Provider.Ails.params)  # supported args

with open("cache.txt", "r", encoding="Windows-1252") as file:
    content = file.read()

response = g4f.ChatCompletion.create(
    model="gpt-3.5-turbo",
    messages=[{"role": "user", "content": content}],
)
print(response)
with open("response.txt", "w", encoding="Windows-1252") as response_file:
    response_file.write(response)
